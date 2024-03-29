package com.gtnewhorizons.modularui.api;

import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.drawable.AdaptableUITexture;
import com.gtnewhorizons.modularui.api.drawable.UITexture;

public class ModularUITextures {

    public static final UITexture ICON_INFO = UITexture.fullImage(ModularUI.MODID, "gui/widgets/information");
    public static final UITexture VANILLA_BACKGROUND = AdaptableUITexture
            .of(ModularUI.MODID, "gui/background/vanilla_background", 195, 136, 4);
    public static final AdaptableUITexture BACKGROUND_BORDER_1PX = AdaptableUITexture
            .of(ModularUI.MODID, "gui/background/background_border_1px", 8, 8, 1);
    public static final AdaptableUITexture BASE_BUTTON = AdaptableUITexture
            .of(ModularUI.MODID, "gui/widgets/base_button", 18, 18, 1);
    public static final AdaptableUITexture ITEM_SLOT = AdaptableUITexture
            .of(ModularUI.MODID, "gui/slot/item", 18, 18, 1);
    public static final AdaptableUITexture FLUID_SLOT = AdaptableUITexture
            .of(ModularUI.MODID, "gui/slot/fluid", 18, 18, 1);

    public static final UITexture ARROW_LEFT = UITexture.fullImage(ModularUI.MODID, "gui/icons/arrow_left");
    public static final UITexture ARROW_RIGHT = UITexture.fullImage(ModularUI.MODID, "gui/icons/arrow_right");
    public static final UITexture ARROW_UP = UITexture.fullImage(ModularUI.MODID, "gui/icons/arrow_up");
    public static final UITexture ARROW_DOWN = UITexture.fullImage(ModularUI.MODID, "gui/icons/arrow_down");
    public static final UITexture CROSS = UITexture.fullImage(ModularUI.MODID, "gui/icons/cross");
    public static final UITexture ARROW_GRAY_LEFT = UITexture.fullImage(ModularUI.MODID, "gui/icons/arrow_gray_left");
    public static final UITexture ARROW_GRAY_RIGHT = UITexture.fullImage(ModularUI.MODID, "gui/icons/arrow_gray_right");
    public static final UITexture ARROW_GRAY_UP = UITexture.fullImage(ModularUI.MODID, "gui/icons/arrow_gray_up");
    public static final UITexture ARROW_GRAY_DOWN = UITexture.fullImage(ModularUI.MODID, "gui/icons/arrow_gray_down");
    public static final UITexture CROSS_GRAY = UITexture.fullImage(ModularUI.MODID, "gui/icons/cross_gray");

    public static final UITexture VANILLA_TAB_TOP = UITexture.fullImage(ModularUI.MODID, "gui/tab/tabs_top");
    public static final UITexture VANILLA_TAB_BOTTOM = UITexture.fullImage(ModularUI.MODID, "gui/tab/tabs_bottom");
    public static final UITexture VANILLA_TAB_LEFT = UITexture.fullImage(ModularUI.MODID, "gui/tab/tabs_left");
    public static final UITexture VANILLA_TAB_RIGHT = UITexture.fullImage(ModularUI.MODID, "gui/tab/tabs_right");

    public static final UITexture VANILLA_TAB_TOP_START = VANILLA_TAB_TOP.getSubArea(0f, 0f, 1 / 3f, 1f);
    public static final UITexture VANILLA_TAB_TOP_MIDDLE = VANILLA_TAB_TOP.getSubArea(1 / 3f, 0f, 2 / 3f, 1f);
    public static final UITexture VANILLA_TAB_TOP_END = VANILLA_TAB_TOP.getSubArea(2 / 3f, 0f, 1f, 1f);

    public static final AdaptableUITexture VANILLA_BUTTON_DISABLED = AdaptableUITexture
            .of(ModularUI.MODID, "gui/widgets/vanilla_button_disabled", 20, 20, 2);
    public static final AdaptableUITexture VANILLA_BUTTON_NORMAL = AdaptableUITexture
            .of(ModularUI.MODID, "gui/widgets/vanilla_button_normal", 20, 20, 2);
    public static final AdaptableUITexture VANILLA_BUTTON_HOVERED = AdaptableUITexture
            .of(ModularUI.MODID, "gui/widgets/vanilla_button_hovered", 20, 20, 2);
}
