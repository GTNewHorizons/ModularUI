package com.gtnewhorizons.modularui.api.forge;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import net.minecraft.nbt.NBTBase;

public interface INBTSerializable<T extends NBTBase> {

    T serializeNBT();

    void deserializeNBT(T var1);
}
