package com.mrbysco.telepastries.item;

import com.mrbysco.telepastries.blocks.cake.BlockCakeBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class CakeBlockItem extends BlockItem {
    public CakeBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        PlayerEntity player = context.getPlayer();
        ShapeContext shapeContext = player == null ? ShapeContext.absent() : ShapeContext.of(player);
        boolean allowed = (!checkStatePlacement() || state.canPlaceAt(context.getWorld(), context.getBlockPos()))
                && context.getWorld().canPlace(state, context.getBlockPos(), shapeContext);
        if (!allowed && player != null && !context.getWorld().isClient) {
            RegistryKey<World> destination = ((BlockCakeBase) getBlock()).getCakeWorld();
            String key = destination != null && destination.equals(player.getWorld().getRegistryKey())
                    ? "telepastries.same_dimension" : "telepastries.teleport_restricted";
            player.sendMessage(Text.translatable(key), true);
        }
        return allowed;
    }
}
