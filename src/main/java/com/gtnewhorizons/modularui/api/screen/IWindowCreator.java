package com.gtnewhorizons.modularui.api.screen;

import net.minecraft.entity.player.EntityPlayer;

public interface IWindowCreator {

    ModularWindow create(EntityPlayer player);
}
