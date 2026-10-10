package com.mrbysco.telepastries.data;

import net.minecraft.nbt.NbtCompound;

public interface TelePastriesPlayerData {
    NbtCompound telepastries$getDimensionPositions();

    void telepastries$setDimensionPositions(NbtCompound positions);
}
