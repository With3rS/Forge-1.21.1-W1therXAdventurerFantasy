package com.w1therx.adventurerfantasy.block;

import com.w1therx.adventurerfantasy.common.enums.ElementType;
import com.w1therx.adventurerfantasy.particle.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlightPrinciple extends Block {
    public static final VoxelShape SHAPE = Block.box(6, 6, 6, 10, 10, 10);

    public BlightPrinciple(Properties properties) {
        super (properties);
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHitResult) {
        if (pPlayer instanceof ServerPlayer && !(pPlayer.getServer() == null) && pLevel instanceof ServerLevel server) {
            server.sendParticles(ModParticles.CONTAMINATED_PARTICLES.get(), pPos.getX()+0.5, pPos.getY()+0.5, pPos.getZ()+0.5, 1, Math.random() * 0.5, Math.random(), Math.random() * 0.5, Math.random() * 0.5 + 0.1);
            server.sendParticles(ModParticles.CONTAMINATED_PARTICLES.get(), pPos.getX()+0.5, pPos.getY()+0.5, pPos.getZ()+0.5, 1, Math.random() * 0.5, Math.random(), Math.random() * 0.5, Math.random() * 0.5 + 0.1);
            server.sendParticles(ModParticles.CONTAMINATED_PARTICLES.get(), pPos.getX()+0.5, pPos.getY()+0.5, pPos.getZ()+0.5, 1, Math.random() * 0.5, Math.random(), Math.random() * 0.5, Math.random() * 0.5 + 0.1);
            pLevel.playSound(null, pPos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.5F, 1.0F);
            pPlayer.getServer().getPlayerList().broadcastSystemMessage(Component.literal("<Voice of Blight> 'Brew poison not to kill, but to savor life.'").withStyle(Style.EMPTY.withColor(ElementType.BLIGHT.getColor())), true);
        }
        return InteractionResult.SUCCESS;
    }
}
