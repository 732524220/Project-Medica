package com.example.medica.block.entity;

import com.example.medica.block.MintTeaCauldronBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 保存已经投入材料的批次与加热进度；不依赖玩家自己计时。 */
public class MintTeaCauldronBlockEntity extends BlockEntity {
    public static final int MINT_PER_BATCH = 2;
    public static final int BREW_TIME_TICKS = 20 * 20;

    private int brewTicks;
    private boolean brewing;

    public MintTeaCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MINT_TEA_CAULDRON, pos, state);
    }

    public void beginBrewing() {
        brewTicks = 0;
        brewing = true;
        setChanged();
    }

    public int getBrewTicks() {
        return brewTicks;
    }

    public Component getStatusMessage() {
        if (getBlockState().getValue(MintTeaCauldronBlock.READY)) {
            return Component.translatable("message.project-medica.tea.ready",
                    getBlockState().getValue(MintTeaCauldronBlock.SERVINGS));
        }
        if (!brewing) {
            return Component.translatable("message.project-medica.tea.no_ingredients");
        }

        int secondsLeft = (BREW_TIME_TICKS - brewTicks + 19) / 20;
        String key = level != null && MintTeaCauldronBlock.hasHeatSource(level, worldPosition)
                ? "message.project-medica.tea.brewing"
                : "message.project-medica.tea.paused";
        return Component.translatable(key, secondsLeft);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  MintTeaCauldronBlockEntity tea) {
        if (!(level instanceof ServerLevel serverLevel)
                || !tea.brewing || state.getValue(MintTeaCauldronBlock.READY)) {
            return;
        }
        // 缺少热源时不增加进度；区块未加载或退出游戏时也不会计时。
        if (!MintTeaCauldronBlock.hasHeatSource(level, pos)) {
            return;
        }

        tea.brewTicks++;
        // 每次进度变化都标记存档，退出重进不会丢失已经加热的时间。
        tea.setChanged();
        if (tea.brewTicks % 20 == 0) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5,
                    2, 0.18, 0.04, 0.18, 0.005);
        }

        if (tea.brewTicks >= BREW_TIME_TICKS) {
            tea.brewTicks = BREW_TIME_TICKS;
            tea.brewing = false;
            level.setBlockAndUpdate(pos, state.setValue(MintTeaCauldronBlock.READY, true));
            level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW,
                    SoundSource.BLOCKS, 0.8F, 1.1F);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                    6, 0.2, 0.1, 0.2, 0.02);
            serverLevel.players().stream()
                    .filter(player -> player.distanceToSqr(pos.getX() + 0.5,
                            pos.getY() + 0.5, pos.getZ() + 0.5) <= 64)
                    .forEach(player -> player.displayClientMessage(
                            Component.translatable("message.project-medica.tea.finished"), true));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("BrewTicks", brewTicks);
        tag.putBoolean("Brewing", brewing);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        brewTicks = Math.clamp(tag.getInt("BrewTicks"), 0, BREW_TIME_TICKS);
        brewing = tag.getBoolean("Brewing");
    }
}
