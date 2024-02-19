package com.gtnewhorizons.modularui;

import java.util.function.Function;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.launchwrapper.Launch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.internal.JsonLoader;
import com.gtnewhorizons.modularui.common.internal.network.NetworkHandler;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;
import com.gtnewhorizons.modularui.common.widget.WidgetJsonRegistry;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(
        modid = ModularUI.MODID,
        version = Tags.VERSION,
        name = "ModularUI",
        acceptedMinecraftVersions = "[1.7.10]",
        dependencies = ModularUI.DEPENDENCIES,
        guiFactory = ModularUI.GUI_FACTORY)
public class ModularUI {

    public static final String MODID = "modularui";
    public static final String DEPENDENCIES = "required-after:gtnhmixins@[2.0.1,); "
            + "required-after:NotEnoughItems@[2.3.50-GTNH,);"
            + "after:hodgepodge@[2.0.0,);"
            + "before:gregtech";
    public static final String GUI_FACTORY = "com.gtnewhorizons.modularui.config.GuiFactory";

    public static final Logger logger = LogManager.getLogger(MODID);

    public static final String MODID_GT5U = "gregtech";
    public static final String MODID_GT6 = "gregapi_post";
    public static final boolean isGT5ULoaded = Loader.isModLoaded(MODID_GT5U) && !Loader.isModLoaded(MODID_GT6);
    public static final boolean isHodgepodgeLoaded = Loader.isModLoaded("hodgepodge");
    public static final boolean isAE2Loaded = Loader.isModLoaded("appliedenergistics2");
    public static final boolean isGTNHLibLoaded = Loader.isModLoaded("gtnhlib");

    public static final boolean isDevEnv = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    @Mod.Instance(ModularUI.MODID)
    public static ModularUI INSTANCE;

    @SidedProxy(
            modId = MODID,
            clientSide = "com.gtnewhorizons.modularui.ClientProxy",
            serverSide = "com.gtnewhorizons.modularui.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        NetworkHandler.init();
        UIInfos.init();
        WidgetJsonRegistry.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
            JsonLoader.loadJson();
        }
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    public static ModularUIContainer createContainer(EntityPlayer player,
            Function<UIBuildContext, ModularWindow> windowCreator, Runnable onWidgetUpdate) {
        UIBuildContext buildContext = new UIBuildContext(player);
        ModularWindow window = windowCreator.apply(buildContext);
        return new ModularUIContainer(new ModularUIContext(buildContext, onWidgetUpdate), window);
    }

    @SideOnly(Side.CLIENT)
    public static ModularGui createGuiScreen(EntityPlayer player,
            Function<UIBuildContext, ModularWindow> windowCreator) {
        return new ModularGui(createContainer(player, windowCreator, null));
    }
}
