package com.example.medica.block;

import com.example.medica.item.ModItems;
import com.example.medica.block.entity.MintTeaCauldronBlockEntity;
import com.example.medica.block.entity.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class MintTeaCauldronBlock extends AbstractCauldronBlock implements EntityBlock {

    public static final IntegerProperty SERVINGS =
            IntegerProperty.create("servings", 1, 4);

    public static final BooleanProperty READY =
            BooleanProperty.create("ready");

    public static final MapCodec<MintTeaCauldronBlock> CODEC =
            simpleCodec(MintTeaCauldronBlock::new);

    private static final CauldronInteraction.InteractionMap INTERACTIONS =
            CauldronInteraction.newInteractionMap("project-medica:mint_tea");

    public MintTeaCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, INTERACTIONS);
        registerDefaultState(
                stateDefinition.any()
                        .setValue(SERVINGS, 4)
                        .setValue(READY, false)
        );
    }

    public static boolean hasHeatSource(Level level, BlockPos pos) {
        BlockState belowState = level.getBlockState(pos.below());
        return CampfireBlock.isLitCampfire(belowState)
                || belowState.is(Blocks.MAGMA_BLOCK);
    }

    public static void registerInteractions() {
        INTERACTIONS.map().put(
                Items.GLASS_BOTTLE,
                (state, level, pos, player, hand, stack) -> {
                    if (!level.isClientSide()) {
                        if (!state.getValue(READY)) {
                            showStatus(level, pos, player);
                            return InteractionResult.SUCCESS;
                        }
                        level.playSound(null, pos, SoundEvents.BOTTLE_FILL,
                                SoundSource.BLOCKS, 1.0F, 1.0F);
                        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

                        // 原版工具负责消耗空瓶，并处理成品入包或背包满时掉落。
                        ItemStack result = ItemUtils.createFilledResult(
                                stack,
                                player,
                                new ItemStack(ModItems.MINT_TEA)
                        );
                        player.setItemInHand(hand, result);

                        int servings = state.getValue(SERVINGS);
                        if (servings > 1) {
                            // 减少一份，方块状态会让液面随之降低。
                            level.setBlockAndUpdate(
                                    pos,
                                    state.setValue(SERVINGS, servings - 1)
                            );
                        } else {
                            // SERVINGS 不能为 0，取完最后一份后换成原版空锅。
                            level.setBlockAndUpdate(
                                    pos,
                                    Blocks.CAULDRON.defaultBlockState()
                            );
                        }
                    }

                    return InteractionResult.SUCCESS;
                }
        );

        CauldronInteraction.WATER.map().put(
                ModItems.MINT,
                (state, level, pos, player, hand, stack) -> {
                    if (!level.isClientSide()) {
                        Component message;

                        if (state.getValue(LayeredCauldronBlock.LEVEL)
                                < LayeredCauldronBlock.MAX_FILL_LEVEL) {
                            message = Component.translatable("message.project-medica.tea.need_water");
                        } else if (!hasHeatSource(level, pos)) {
                            message = Component.translatable("message.project-medica.tea.need_heat");
                        } else if (!player.hasInfiniteMaterials()
                                && stack.getCount() < MintTeaCauldronBlockEntity.MINT_PER_BATCH) {
                            message = Component.translatable("message.project-medica.tea.need_mint",
                                    MintTeaCauldronBlockEntity.MINT_PER_BATCH);
                        } else {
                            // 确认新锅和方块实体已创建，才扣除这批材料。
                            BlockState teaState = ModBlocks.MINT_TEA_CAULDRON.defaultBlockState()
                                    .setValue(SERVINGS, 4).setValue(READY, false);
                            if (!level.setBlockAndUpdate(pos, teaState)) {
                                return InteractionResult.FAIL;
                            }
                            if (!(level.getBlockEntity(pos) instanceof MintTeaCauldronBlockEntity tea)) {
                                level.setBlockAndUpdate(pos, state);
                                return InteractionResult.FAIL;
                            }
                            stack.consume(MintTeaCauldronBlockEntity.MINT_PER_BATCH, player);
                            tea.beginBrewing();
                            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY,
                                    SoundSource.BLOCKS, 0.7F, 1.0F);
                            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                            message = Component.translatable("message.project-medica.tea.started",
                                    MintTeaCauldronBlockEntity.BREW_TIME_TICKS / 20);
                        }

                        player.displayClientMessage(
                                message,
                                true
                        );
                    }

                    return InteractionResult.SUCCESS;
                }
        );
    }

    private static void showStatus(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof MintTeaCauldronBlockEntity tea) {
            player.displayClientMessage(tea.getStatusMessage(), true);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            showStatus(level, pos, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                         BlockPos pos, Player player, InteractionHand hand,
                                         BlockHitResult hit) {
        if (stack.is(Items.GLASS_BOTTLE)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        // 再次投入薄荷等物品只查看状态，不会重复消耗材料或重置进度。
        if (!level.isClientSide()) {
            showStatus(level, pos, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MintTeaCauldronBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.MINT_TEA_CAULDRON) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof MintTeaCauldronBlockEntity tea) {
                MintTeaCauldronBlockEntity.serverTick(tickLevel, pos, tickState, tea);
            }
        };
    }

    @Override
    protected MapCodec<? extends AbstractCauldronBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SERVINGS, READY);
    }

    @Override
    public boolean isFull(BlockState state) {
        return state.getValue(SERVINGS) == 4;
    }
}
