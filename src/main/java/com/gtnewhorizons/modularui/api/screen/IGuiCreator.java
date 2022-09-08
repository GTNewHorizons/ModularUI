package com.gtnewhorizons.modularui.api.screen;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

@FunctionalInterface
public interface IGuiCreator {

    @SideOnly(Side.CLIENT)
    Object create(EntityPlayer player, World world, int x, int y, int z);

}
