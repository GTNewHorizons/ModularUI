package com.gtnewhorizons.modularui.mixins;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.IntHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface KeyBindAccess {

    @Accessor
    IntHashMap getHash();

    @Accessor
    void setPressed(boolean pressed);

    @Accessor
    int getPressTime();

    @Accessor
    void setPressTime(int time);
}
