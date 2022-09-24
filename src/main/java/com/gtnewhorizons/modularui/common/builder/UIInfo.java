package com.gtnewhorizons.modularui.common.builder;

import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.api.screen.IContainerCreator;
import com.gtnewhorizons.modularui.api.screen.IGuiCreator;
import com.gtnewhorizons.modularui.common.internal.InternalUIMapper;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import java.util.function.Function;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Responsible for registering actual Container and GuiContainer, and opening them.
 */
public class UIInfo<CC extends IContainerCreator, GC extends IGuiCreator> {

    private final int id;
    private final CC containerCreator;
    private final GC guiCreator;

    /**
     * @param containerCreator {@link IContainerCreator}
     * @param guiCreator {@link IGuiCreator}
     */
    UIInfo(CC containerCreator, GC guiCreator) {
        this.id = InternalUIMapper.getInstance().register(containerCreator, guiCreator);
        this.containerCreator = containerCreator;
        this.guiCreator = guiCreator;
    }

    /**
     * Open GUI of TileEntity at given position.
     * This should be called only by logical server.
     * For client-only GUI, use {@link UIInfos#openClientUI(EntityPlayer, Function)}
     */
    public void open(EntityPlayer player, World world, int x, int y, int z) {
        if (NetworkUtils.isClient()) {
            ModularUI.logger.warn("Please use UIInfos.openClientUI to open a client only ui!");
        }
        FMLNetworkHandler.openGui(player, ModularUI.INSTANCE, id, world, x, y, z);
    }

    /**
     * Open GUI of TileEntity at given position.
     * This should be called only by logical server.
     * For client-only GUI, use {@link UIInfos#openClientUI(EntityPlayer, Function)}
     */
    public void open(EntityPlayer player, World world, Vec3 pos) {
        open(player, world, (int) pos.xCoord, (int) pos.yCoord, (int) pos.zCoord);
    }

    public void open(EntityPlayer player) {
        open(player, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
    }
}
