package com.example.medica.block;

import com.example.medica.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MintCropBlock extends CropBlock {
    public static final MapCodec<MintCropBlock> CODEC = simpleCodec(MintCropBlock::new);

    private static final VoxelShape[] SHAPES = {
            Block.box(5, 0, 5, 11, 6, 11),
            Block.box(4, 0, 4, 12, 9, 12),
            Block.box(3, 0, 3, 13, 12, 13),
            Block.box(2, 0, 2, 14, 16, 14)
    };

    public MintCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<MintCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        // 同一个薄荷物品既是收获物，也是种植材料。
        return ModItems.MINT;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        int age = getAge(state);
        int stage = age == getMaxAge() ? 3 : Math.min(age / 2, 2);
        return SHAPES[stage];
    }
}
