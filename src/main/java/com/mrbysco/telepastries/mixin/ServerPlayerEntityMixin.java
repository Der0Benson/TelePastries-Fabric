package com.mrbysco.telepastries.mixin;

import com.mrbysco.telepastries.data.TelePastriesPlayerData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements TelePastriesPlayerData {
    @Unique
    private static final String TELEPASTRIES_POSITIONS = "telepastries:dimension_positions";

    @Unique
    private NbtCompound telepastries$dimensionPositions = new NbtCompound();

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void telepastries$readPositions(NbtCompound nbt, CallbackInfo callbackInfo) {
        if (nbt.contains(TELEPASTRIES_POSITIONS, NbtElement.COMPOUND_TYPE)) {
            telepastries$dimensionPositions = nbt.getCompound(TELEPASTRIES_POSITIONS).copy();
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void telepastries$writePositions(NbtCompound nbt, CallbackInfo callbackInfo) {
        nbt.put(TELEPASTRIES_POSITIONS, telepastries$dimensionPositions.copy());
    }

    @Override
    public NbtCompound telepastries$getDimensionPositions() {
        return telepastries$dimensionPositions;
    }

    @Override
    public void telepastries$setDimensionPositions(NbtCompound positions) {
        telepastries$dimensionPositions = positions.copy();
    }
}
