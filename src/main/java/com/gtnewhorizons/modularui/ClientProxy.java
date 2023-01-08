package com.gtnewhorizons.modularui;

import codechicken.nei.guihook.GuiContainerManager;
import com.gtnewhorizons.modularui.api.drawable.FallbackableUITexture;
import com.gtnewhorizons.modularui.common.internal.JsonLoader;
import com.gtnewhorizons.modularui.common.peripheral.PeripheralInputHandler;
import com.gtnewhorizons.modularui.integration.nei.ModularUIContainerObjectHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.SimpleReloadableResourceManager;

@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        if (ModularUI.isNEILoaded) {
            GuiContainerManager.addInputHandler(new PeripheralInputHandler());
            GuiContainerManager.addObjectHandler(new ModularUIContainerObjectHandler());
        }
    }

    public void postInit() {
        super.postInit();
        ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                .registerReloadListener(new ResourceManagerReloadListener());
    }

    private static class ResourceManagerReloadListener implements IResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(IResourceManager p_110549_1_) {
            ModularUI.logger.info("Reloading GUIs");
            JsonLoader.loadJson();
            FallbackableUITexture.reload();
        }
    }
}
