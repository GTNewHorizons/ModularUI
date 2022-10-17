package com.gtnewhorizons.modularui.config;

import com.gtnewhorizons.modularui.ModularUI;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

@SuppressWarnings("rawtypes")
public class ModularUIGuiConfig extends GuiConfig {

    public ModularUIGuiConfig(GuiScreen parentScreen) {
        super(
                parentScreen,
                getConfigElements(),
                ModularUI.MODID,
                false,
                false,
                GuiConfig.getAbridgedConfigPath(Config.config.toString()));
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<>();

        for (String category : Config.CATEGORIES) {
            list.add(new ConfigElement(Config.config.getCategory(category.toLowerCase(Locale.US))));
        }

        return list;
    }
}
