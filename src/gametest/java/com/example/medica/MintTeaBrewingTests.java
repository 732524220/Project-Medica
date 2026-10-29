package com.example.medica;

import com.example.medica.block.MintTeaCauldronBlock;
import com.example.medica.block.ModBlocks;
import com.example.medica.block.entity.MintTeaCauldronBlockEntity;
import com.example.medica.item.ModItems;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class MintTeaBrewingTests implements FabricGameTest {
    private static final BlockPos POT = new BlockPos(3, 2, 3);

    private static void waterPot(GameTestHelper helper, int water, boolean lit) {
        helper.setBlock(POT.below(), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, lit));
        helper.setBlock(POT, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, water));
    }

    private static Player player(GameTestHelper helper, GameType gameType, Item item, int count) {
        Player player = helper.makeMockPlayer(gameType);
        // 原版 mock 只覆写 isCreative；物品消耗使用的能力需要显式设置。
        gameType.updatePlayerAbilities(player.getAbilities());
        BlockPos pos = helper.absolutePos(POT);
        player.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item, count));
        return player;
    }

    private static BlockState state(GameTestHelper helper) {
        return helper.getLevel().getBlockState(helper.absolutePos(POT));
    }

    private static MintTeaCauldronBlockEntity tea(GameTestHelper helper) {
        BlockEntity entity = helper.getLevel().getBlockEntity(helper.absolutePos(POT));
        helper.assertTrue(entity instanceof MintTeaCauldronBlockEntity, "Tea block entity must exist");
        return (MintTeaCauldronBlockEntity) entity;
    }

    private static int count(Player player, Item item) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 500)
    public void automaticallyBrewsAndBottlesFourServings(GameTestHelper helper) {
        waterPot(helper, 3, true);
        Player player = player(helper, GameType.SURVIVAL, ModItems.MINT, 6);
        helper.useBlock(POT, player);
        helper.assertTrue(state(helper).is(ModBlocks.MINT_TEA_CAULDRON), "Water must become tea pot");
        helper.assertTrue(count(player, ModItems.MINT) == 4, "Start must consume exactly two mint");
        helper.assertFalse(state(helper).getValue(MintTeaCauldronBlock.READY), "Tea cannot be ready immediately");

        helper.runAtTickTime(100, () -> {
            int progress = tea(helper).getBrewTicks();
            helper.assertTrue(progress > 0 && progress < 400, "Server ticker must advance brewing");
            helper.useBlock(POT, player);
            helper.assertTrue(tea(helper).getBrewTicks() == progress, "Repeated mint use must not reset progress");
            helper.assertTrue(count(player, ModItems.MINT) == 4, "Repeated use must not consume more mint");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, 4));
            helper.useBlock(POT, player);
            helper.assertTrue(count(player, Items.GLASS_BOTTLE) == 4, "Unready tea must not consume bottles");
            helper.assertTrue(count(player, ModItems.MINT_TEA) == 0, "Unready tea must not give drinks");
        });
        helper.runAtTickTime(399, () ->
                helper.assertFalse(state(helper).getValue(MintTeaCauldronBlock.READY), "Must heat for full 400 ticks"));
        helper.runAtTickTime(420, () -> {
            helper.assertTrue(state(helper).getValue(MintTeaCauldronBlock.READY), "Tea must finish automatically");
            helper.setBlock(POT.below(), Blocks.AIR);
            for (int serving = 1; serving <= 4; serving++) {
                helper.useBlock(POT, player);
                helper.assertTrue(count(player, ModItems.MINT_TEA) == serving, "Each bottle must yield one tea");
                helper.assertTrue(count(player, Items.GLASS_BOTTLE) == 4 - serving, "Consume one bottle per serving");
                if (serving < 4) {
                    helper.assertTrue(state(helper).getValue(MintTeaCauldronBlock.SERVINGS) == 4 - serving,
                            "Liquid must go down one serving");
                    helper.assertTrue(state(helper).getValue(MintTeaCauldronBlock.READY), "Remaining tea stays ready");
                }
            }
            helper.assertTrue(state(helper).is(Blocks.CAULDRON), "Last serving must leave vanilla empty pot");
            helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(POT)) == null,
                    "Empty pot must not retain a brewing entity");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 700)
    public void magmaHeatsPausesAndResumesWithEitherSource(GameTestHelper helper) {
        waterPot(helper, 3, false);
        helper.setBlock(POT.below(), Blocks.MAGMA_BLOCK);
        Player player = player(helper, GameType.SURVIVAL, ModItems.MINT, 4);
        helper.useBlock(POT, player);
        helper.assertTrue(state(helper).is(ModBlocks.MINT_TEA_CAULDRON), "Magma must allow brewing to start");
        helper.assertTrue(count(player, ModItems.MINT) == 2, "Magma batch must consume exactly two mint");
        int[] savedProgress = {0};

        helper.runAtTickTime(100, () -> {
            savedProgress[0] = tea(helper).getBrewTicks();
            helper.assertTrue(savedProgress[0] > 0, "Magma must advance brewing");
            helper.setBlock(POT.below(), Blocks.STONE);
        });
        helper.runAtTickTime(200, () -> {
            helper.assertTrue(tea(helper).getBrewTicks() == savedProgress[0], "Removing magma must pause progress");
            helper.assertFalse(state(helper).getValue(MintTeaCauldronBlock.READY), "Paused batch cannot finish");
            helper.setBlock(POT.below(), Blocks.MAGMA_BLOCK);
        });
        helper.runAtTickTime(250, () -> {
            helper.assertTrue(tea(helper).getBrewTicks() > savedProgress[0], "Restoring magma must resume progress");
            helper.setBlock(POT.below(), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        });
        helper.runAtTickTime(300, () -> {
            helper.assertTrue(tea(helper).getBrewTicks() > savedProgress[0] + 50, "Campfire must continue the same batch");
            helper.setBlock(POT.below(), Blocks.MAGMA_BLOCK);
        });
        helper.runAtTickTime(499, () ->
                helper.assertFalse(state(helper).getValue(MintTeaCauldronBlock.READY), "Magma uses the same full heating time"));
        helper.runAtTickTime(520, () -> {
            helper.assertTrue(state(helper).getValue(MintTeaCauldronBlock.READY), "Magma must finish brewing automatically");
            helper.assertTrue(tea(helper).getBrewTicks() == 400, "Switching heat sources must preserve heating time");
            helper.setBlock(POT.below(), Blocks.AIR);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, 4));
            for (int serving = 0; serving < 4; serving++) {
                helper.useBlock(POT, player);
            }
            helper.assertTrue(count(player, ModItems.MINT_TEA) == 4, "Magma batch must yield four bottles");
            helper.assertTrue(count(player, Items.GLASS_BOTTLE) == 0, "Four servings must consume four bottles");
            helper.assertTrue(state(helper).is(Blocks.CAULDRON), "Finished batch leaves an empty pot");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 700)
    public void pausesPersistsAndResumes(GameTestHelper helper) {
        waterPot(helper, 3, true);
        Player player = player(helper, GameType.SURVIVAL, ModItems.MINT, 2);
        helper.useBlock(POT, player);
        int[] savedProgress = {0};
        helper.runAtTickTime(100, () -> {
            helper.setBlock(POT.below(), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
            savedProgress[0] = tea(helper).getBrewTicks();
            helper.assertTrue(savedProgress[0] > 0, "Must have progress before pause");
            // 使用游戏真实 NBT 保存/加载路径重新创建方块实体。
            CompoundTag saved = tea(helper).saveWithFullMetadata(helper.getLevel().registryAccess());
            BlockPos pos = helper.absolutePos(POT);
            BlockState state = state(helper);
            helper.getLevel().removeBlockEntity(pos);
            BlockEntity restored = BlockEntity.loadStatic(pos, state, saved, helper.getLevel().registryAccess());
            helper.assertTrue(restored instanceof MintTeaCauldronBlockEntity, "Registered entity must reload from NBT");
            helper.getLevel().setBlockEntity(restored);
            helper.assertTrue(tea(helper).getBrewTicks() == savedProgress[0], "Saved progress must survive loading");
        });
        helper.runAtTickTime(300, () -> {
            helper.assertTrue(tea(helper).getBrewTicks() == savedProgress[0], "No heat must pause rather than reset");
            helper.assertFalse(state(helper).getValue(MintTeaCauldronBlock.READY), "Paused tea stays unready");
            helper.setBlock(POT.below(), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        });
        helper.runAtTickTime(620, () -> {
            helper.assertTrue(state(helper).getValue(MintTeaCauldronBlock.READY), "Relit pot must resume saved batch");
            helper.assertTrue(tea(helper).getBrewTicks() == 400, "Finished progress is capped");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void invalidInputsPreserveWaterAndMint(GameTestHelper helper) {
        Player player = player(helper, GameType.SURVIVAL, ModItems.MINT, 2);
        waterPot(helper, 2, true);
        helper.useBlock(POT, player);
        helper.assertTrue(state(helper).is(Blocks.WATER_CAULDRON), "Partial water cannot brew");
        helper.assertTrue(state(helper).getValue(LayeredCauldronBlock.LEVEL) == 2, "Rejected water level unchanged");
        helper.assertTrue(count(player, ModItems.MINT) == 2, "Partial water must not take mint");
        waterPot(helper, 3, false);
        helper.useBlock(POT, player);
        helper.assertTrue(state(helper).is(Blocks.WATER_CAULDRON), "Unlit campfire cannot start brewing");
        helper.assertTrue(count(player, ModItems.MINT) == 2, "No heat must not take mint");
        helper.setBlock(POT.below(), Blocks.STONE);
        helper.useBlock(POT, player);
        helper.assertTrue(state(helper).is(Blocks.WATER_CAULDRON), "Stone cannot start brewing");
        helper.assertTrue(count(player, ModItems.MINT) == 2, "Stone must not take mint");
        helper.setBlock(POT.below(), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        helper.useBlock(POT, player);
        helper.assertTrue(state(helper).is(Blocks.WATER_CAULDRON), "Unlit soul campfire cannot start brewing");
        helper.assertTrue(count(player, ModItems.MINT) == 2, "Unlit soul campfire must not take mint");
        waterPot(helper, 3, true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.MINT));
        helper.useBlock(POT, player);
        helper.assertTrue(state(helper).is(Blocks.WATER_CAULDRON), "One mint is insufficient");
        helper.assertTrue(count(player, ModItems.MINT) == 1, "Insufficient mint stays in hand");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        helper.useBlock(POT, player);
        helper.assertTrue(state(helper).is(Blocks.WATER_CAULDRON), "Unrelated item cannot start brewing");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 470)
    public void commandPlacedUnreadyPotDoesNotBrewFreeTea(GameTestHelper helper) {
        helper.setBlock(POT.below(), Blocks.CAMPFIRE);
        helper.setBlock(POT, ModBlocks.MINT_TEA_CAULDRON.defaultBlockState());
        helper.runAtTickTime(430, () -> {
            helper.assertFalse(state(helper).getValue(MintTeaCauldronBlock.READY), "No paid batch must stay unready");
            helper.assertTrue(tea(helper).getBrewTicks() == 0, "No ingredients means no automatic progress");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    @SuppressWarnings("removal") // 1.21.4 的基础 mock 不支持真实掉落，使用此版本的服务端测试接口。
    public void fullInventoryDropsExactlyOneTea(GameTestHelper helper) {
        helper.setBlock(POT, ModBlocks.MINT_TEA_CAULDRON.defaultBlockState().setValue(MintTeaCauldronBlock.READY, true));
        // 基础 mock Player.drop() 不生成实体；这里必须用服务端玩家验证真实掉落。
        Player player = helper.makeMockServerPlayerInLevel();
        GameType.SURVIVAL.updatePlayerAbilities(player.getAbilities());
        BlockPos absolute = helper.absolutePos(POT);
        player.setPos(absolute.getX() + 0.5, absolute.getY() + 1, absolute.getZ() + 0.5);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, 2));
        for (int slot = 1; slot < 36; slot++) player.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
        helper.useBlock(POT, player);
        helper.assertTrue(count(player, Items.GLASS_BOTTLE) == 1, "Full inventory still consumes exactly one bottle");
        helper.assertTrue(count(player, ModItems.MINT_TEA) == 0, "No inventory space for tea");
        helper.assertItemEntityCountIs(ModItems.MINT_TEA, POT.above(), 3, 1);
        helper.assertTrue(state(helper).getValue(MintTeaCauldronBlock.SERVINGS) == 3, "Drop uses one serving");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 450)
    public void breakingPotReturnsContainerAndCancelsBatch(GameTestHelper helper) {
        waterPot(helper, 3, true);
        Player player = player(helper, GameType.SURVIVAL, ModItems.MINT, 2);
        helper.useBlock(POT, player);
        helper.runAtTickTime(80, () -> {
            ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
            helper.assertTrue(pickaxe.isCorrectToolForDrops(state(helper)), "Tea pot should be mineable with pickaxe");
            var drops = Block.getDrops(state(helper), helper.getLevel(), helper.absolutePos(POT),
                    tea(helper), player, pickaxe);
            helper.assertTrue(drops.size() == 1 && drops.getFirst().is(Items.CAULDRON)
                    && drops.getFirst().getCount() == 1, "Breaking returns only the empty container");
            helper.setBlock(POT, Blocks.AIR);
            helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(POT)) == null,
                    "Broken pot must remove progress entity");
            waterPot(helper, 3, true);
        });
        helper.runAtTickTime(420, () -> {
            helper.assertTrue(state(helper).is(Blocks.WATER_CAULDRON), "Removed batch cannot finish in replacement pot");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void offhandAndCreativeInteractions(GameTestHelper helper) {
        waterPot(helper, 3, true);
        Player player = player(helper, GameType.SURVIVAL, Items.STICK, 1);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.MINT, 2));
        BlockPos pos = helper.absolutePos(POT);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        state(helper).useItemOn(player.getOffhandItem(), helper.getLevel(), player, InteractionHand.OFF_HAND, hit);
        helper.assertTrue(player.getOffhandItem().isEmpty(), "Offhand mint consumed");
        helper.assertTrue(player.getMainHandItem().is(Items.STICK), "Main hand unchanged");
        helper.getLevel().setBlockAndUpdate(pos, state(helper).setValue(MintTeaCauldronBlock.READY, true));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GLASS_BOTTLE));
        state(helper).useItemOn(player.getOffhandItem(), helper.getLevel(), player, InteractionHand.OFF_HAND, hit);
        helper.assertTrue(player.getOffhandItem().is(ModItems.MINT_TEA), "Single offhand bottle replaced with tea");

        waterPot(helper, 3, true);
        Player creative = player(helper, GameType.CREATIVE, ModItems.MINT, 1);
        helper.useBlock(POT, creative);
        helper.assertTrue(count(creative, ModItems.MINT) == 1, "Creative mint must not be consumed");
        helper.assertTrue(state(helper).is(ModBlocks.MINT_TEA_CAULDRON), "Creative player can brew");
        helper.succeed();
    }
}
