package com.gtnewhorizons.modularui.common.internal;

import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.screen.IContainerCreator;
import com.gtnewhorizons.modularui.api.screen.IGuiCreator;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InternalUIMapper implements IGuiHandler {

    private static InternalUIMapper INSTANCE;

    public static InternalUIMapper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InternalUIMapper();
        }
        return INSTANCE;
    }

    private int id;
    private final List<IContainerCreator> serverContainers;
    private final List<IGuiCreator> clientGuis;

    public InternalUIMapper() {
        if (ModularUI.INSTANCE == null) {
            throw new NullPointerException("Something went wrong! Mod instance should not be null!");
        }
        NetworkRegistry.INSTANCE.registerGuiHandler(ModularUI.INSTANCE, this);
        this.serverContainers = new ArrayList<>();
        this.clientGuis = new ArrayList<>();
    }

    public <CC extends IContainerCreator, GC extends IGuiCreator> int register(CC containerCreator, GC guiCreator) {
        this.serverContainers.add(containerCreator);
        this.clientGuis.add(guiCreator);
        return id++;
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return serverContainers.get(id).create(player, world, x, y, z);
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        Object screen = clientGuis.get(id).create(player, world, x, y, z);
        if (screen != null && !(screen instanceof GuiScreen)) {
            throw new RuntimeException("The returned Object of IGuiCreator must be a instance of GuiScreen!");
        }
        return screen;
    }

}
