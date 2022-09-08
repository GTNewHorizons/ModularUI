package com.gtnewhorizons.modularui;

import com.gtnewhorizons.modularui.config.Config;
import com.gtnewhorizons.modularui.test.TestBlock;
import com.gtnewhorizons.modularui.test.TestTile;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {

    public static Block testBlock;

    public void preInit(FMLPreInitializationEvent event) {

        testBlock = new TestBlock(Material.rock)
            .setBlockName("testBlock")
            .setCreativeTab(CreativeTabs.tabBlock)
            .setBlockTextureName("stone");
        GameRegistry.registerBlock(testBlock, "testBlock");
        GameRegistry.registerTileEntity(TestTile.class, "TestTileEntity");

        Config.init(event.getSuggestedConfigurationFile());

        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void postInit() {
    }

//    @SubscribeEvent
//    public static void registerBlocks(RegistryEvent.Register<Block> event) {
//        IForgeRegistry<Block> registry = event.getRegistry();
//        registry.register(testBlock);
//    }
//
//    @SubscribeEvent
//    public static void registerItems(RegistryEvent.Register<Item> event) {
//        IForgeRegistry<Item> registry = event.getRegistry();
//        registry.register(testItemBlock);
//    }

    @SubscribeEvent
    public void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(ModularUI.MODID)) {
            Config.syncConfig();
        }
    }
}
