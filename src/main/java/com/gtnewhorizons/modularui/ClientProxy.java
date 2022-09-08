package com.gtnewhorizons.modularui;


import codechicken.lib.math.MathHelper;
import com.gtnewhorizons.modularui.common.internal.JsonLoader;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.input.Mouse;

@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    public void postInit() {
        super.postInit();
        ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this::onReload);
    }

    public void onReload(IResourceManager manager) {
        ModularUI.logger.info("Reloading GUIs");
        JsonLoader.loadJson();
    }

    @SubscribeEvent
    public void mouseScreenInput(GuiScreenEvent event) {
        if (event.gui instanceof ModularGui) {
            int w = Mouse.getEventDWheel();
            int wheel = (int)MathHelper.clip(w, -1, 1);
            if (wheel != 0) {
                ((ModularGui) event.gui).mouseScroll(wheel);
            }
        }
    }
}
