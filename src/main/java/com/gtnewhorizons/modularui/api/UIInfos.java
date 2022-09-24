package com.gtnewhorizons.modularui.api;

import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.builder.UIBuilder;
import com.gtnewhorizons.modularui.common.builder.UIInfo;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.function.Function;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class UIInfos {

    public static void init() {}

    public static final UIInfo<?, ?> TILE_MODULAR_UI = UIBuilder.of()
            .gui(((player, world, x, y, z) -> {
                if (!world.isRemote) return null;
                TileEntity te = world.getTileEntity(x, y, z);
                if (te instanceof ITileWithModularUI) {
                    return ModularUI.createGuiScreen(player, ((ITileWithModularUI) te)::createWindow);
                }
                return null;
            }))
            .container((player, world, x, y, z) -> {
                TileEntity te = world.getTileEntity(x, y, z);
                if (te instanceof ITileWithModularUI) {
                    return ModularUI.createContainer(player, ((ITileWithModularUI) te)::createWindow, te::markDirty);
                }
                return null;
            })
            .build();

    @SideOnly(Side.CLIENT)
    public static void openClientUI(EntityPlayer player, Function<UIBuildContext, ModularWindow> uiCreator) {
        if (!NetworkUtils.isClient()) {
            ModularUI.logger.info("Tried opening client ui on server!");
            return;
        }
        UIBuildContext buildContext = new UIBuildContext(player);
        ModularWindow window = uiCreator.apply(buildContext);
        GuiScreen screen =
                new ModularGui(new ModularUIContainer(new ModularUIContext(buildContext, null, true), window));
        FMLCommonHandler.instance().showGuiScreen(screen);
    }

    /**
     * Call this if you want to draw widgets in other mods' GUI.
     * Don't call if you're using {@link UIInfo#open} or {@link #openClientUI}.
     */
    public static void initializeWindow(EntityPlayer player, ModularWindow window) {
        UIBuildContext buildContext = new UIBuildContext(player);
        new ModularGui(new ModularUIContainer(new ModularUIContext(buildContext, null, true), window));
    }
}
