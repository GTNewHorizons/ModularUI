package com.gtnewhorizons.modularui.common.internal;

import java.util.HashMap;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.math.Color;

public class Theme {

    public static final Theme INSTANCE = new Theme();

    public static final String KEY_BACKGROUND = "bg";
    public static final String KEY_BUTTON = "button";
    public static final String KEY_TEXT = "text";
    public static final String KEY_ITEM_SLOT = "itemslot";
    public static final String KEY_FLUID_SLOT = "fluidslot";
    public static final String KEY_SLOT_HIGHLIGHT = "slothighlight";

    private final HashMap<String, Integer> colors = new HashMap<String, Integer>(32);

    private Theme() {
        registerThemeColor(KEY_BACKGROUND);
        registerThemeColor(KEY_BUTTON);
        registerThemeColor(KEY_TEXT, 0x404040);
        registerThemeColor(KEY_ITEM_SLOT);
        registerThemeColor(KEY_FLUID_SLOT);
        registerThemeColor(KEY_SLOT_HIGHLIGHT, Color.withAlpha(Color.WHITE.normal, 0x80));
    }

    public void registerThemeColor(String name) {
        registerThemeColor(name, 0xFFFFFFFF);
    }

    public void registerThemeColor(String name, int value) {
        if (colors.containsKey(name)) {
            ModularUI.logger.error("Theme already has a color with key {}", name);
            return;
        }
        colors.put(name, value);
    }

    public int getColor(@Nullable String key) {
        return key == null ? 0xFFFFFFFF : colors.getOrDefault(key, 0xFFFFFFFF);
    }

    public int getBackground() {
        return getColor(KEY_BACKGROUND);
    }

    public int getButton() {
        return getColor(KEY_BUTTON);
    }

    public int getText() {
        return getColor(KEY_TEXT);
    }

    public int getItemSlot() {
        return getColor(KEY_ITEM_SLOT);
    }

    public int getFluidSlot() {
        return getColor(KEY_FLUID_SLOT);
    }

    public int getSlotHighlight() {
        return getColor(KEY_SLOT_HIGHLIGHT);
    }

    public JsonObject readTheme(JsonObject json) {
        for (String key : colors.keySet()) {
            if (json.has(key)) {
                colors.put(key, json.get(key).getAsInt());
            } else {
                json.addProperty(key, colors.get(key));
            }
        }
        return json;
    }
}
