package com.gtnewhorizons.modularui;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;

import com.gtnewhorizons.modularui.config.Config;
import com.gtnewhorizons.modularui.test.TestBlock;
import com.gtnewhorizons.modularui.test.TestTile;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy {

    public static Block testBlock;

    public void preInit(FMLPreInitializationEvent event) {
        Config.init(event.getSuggestedConfigurationFile());

        if (ModularUI.isDevEnv || Config.forceEnableDebugBlock) {
            testBlock = new TestBlock(Material.rock).setBlockName("testBlock").setCreativeTab(CreativeTabs.tabBlock)
                    .setBlockTextureName("stone");
            GameRegistry.registerBlock(testBlock, "testBlock");
            GameRegistry.registerTileEntity(TestTile.class, "TestTileEntity");
        }

        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void postInit() {}

    @SubscribeEvent
    public void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(ModularUI.MODID)) {
            Config.syncConfig();
        }
    }
}
