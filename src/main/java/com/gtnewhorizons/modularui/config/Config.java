package com.gtnewhorizons.modularui.config;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import com.gtnewhorizons.modularui.ModularUI;

public class Config {

    public static Configuration config;

    public static int openCloseDurationMs = 200;
    public static boolean openCloseFade = false;
    public static boolean openCloseScale = false;
    public static boolean openCloseTranslateFromBottom = false;
    public static boolean openCloseRotateFast = false;

    public static boolean smoothProgressbar = true;
    public static String textCursor = "underscore";

    public static boolean escRestoreLastText = false;
    public static boolean closeWindowsAtOnce = false;

    public static Locale locale = Locale.getDefault();

    public static boolean useJson = false;

    public static boolean debug = false;
    public static boolean forceEnableDebugBlock = false;

    public static final String CATEGORY_ANIMATIONS = "animations";
    public static final String CATEGORY_RENDERING = "rendering";
    public static final String CATEGORY_KEYBOARD = "keyboard";
    public static final String CATEGORY_LOCALIZATION = "localization";
    public static final String CATEGORY_JSON = "json";
    public static final String CATEGORY_DEBUG = "debug";

    private static final String LANG_PREFIX = ModularUI.MODID + ".config.";

    public static final String[] CATEGORIES = new String[] { CATEGORY_ANIMATIONS, CATEGORY_RENDERING, CATEGORY_KEYBOARD,
            CATEGORY_LOCALIZATION, CATEGORY_JSON, CATEGORY_DEBUG, };

    public static void init(File configFile) {
        config = new Configuration(configFile);
        syncConfig();
    }

    public static void syncConfig() {
        config.setCategoryComment(CATEGORY_ANIMATIONS, "Animations");
        config.setCategoryLanguageKey(CATEGORY_ANIMATIONS, LANG_PREFIX + CATEGORY_ANIMATIONS);
        config.setCategoryComment(CATEGORY_RENDERING, "Rendering");
        config.setCategoryLanguageKey(CATEGORY_RENDERING, LANG_PREFIX + CATEGORY_RENDERING);
        config.setCategoryComment(CATEGORY_KEYBOARD, "Keyboard");
        config.setCategoryLanguageKey(CATEGORY_KEYBOARD, LANG_PREFIX + CATEGORY_KEYBOARD);
        config.setCategoryComment(CATEGORY_LOCALIZATION, "Localization");
        config.setCategoryLanguageKey(CATEGORY_LOCALIZATION, LANG_PREFIX + CATEGORY_LOCALIZATION);
        config.setCategoryComment(CATEGORY_JSON, "Json");
        config.setCategoryLanguageKey(CATEGORY_JSON, LANG_PREFIX + CATEGORY_JSON);
        config.setCategoryComment(CATEGORY_DEBUG, "Debug");
        config.setCategoryLanguageKey(CATEGORY_DEBUG, LANG_PREFIX + CATEGORY_DEBUG);

        // === Animations ===

        openCloseDurationMs = config.get(
                CATEGORY_ANIMATIONS,
                "openCloseDurationMs",
                200,
                "How many milliseconds will it take to draw open/close animation",
                0,
                3000).setLanguageKey(LANG_PREFIX + CATEGORY_ANIMATIONS + ".openCloseDurationMs").getInt();

        openCloseFade = config
                .get(
                        CATEGORY_ANIMATIONS,
                        "openCloseFade",
                        false,
                        "Whether to draw fade in/out animation on GUI open/close")
                .setLanguageKey(LANG_PREFIX + CATEGORY_ANIMATIONS + ".openCloseFade").getBoolean();

        openCloseScale = config
                .get(
                        CATEGORY_ANIMATIONS,
                        "openCloseScale",
                        false,
                        "Whether to draw scale in/out animation on GUI open/close")
                .setLanguageKey(LANG_PREFIX + CATEGORY_ANIMATIONS + ".openCloseScale").getBoolean();

        openCloseTranslateFromBottom = config
                .get(
                        CATEGORY_ANIMATIONS,
                        "openCloseTranslateFromBottom",
                        false,
                        "Whether to draw GUI coming out of / going out to the bottom of the screen on GUI open/close")
                .setLanguageKey(LANG_PREFIX + CATEGORY_ANIMATIONS + ".openCloseTranslateFromBottom").getBoolean();

        openCloseRotateFast = config
                .get(
                        CATEGORY_ANIMATIONS,
                        "openCloseRotateFast",
                        false,
                        "Whether to draw GUI rotating fast on GUI open/close")
                .setLanguageKey(LANG_PREFIX + CATEGORY_ANIMATIONS + ".openCloseRotateFast").getBoolean();

        // === Rendering ===

        smoothProgressbar = config.get(CATEGORY_RENDERING, "smoothProgressbar", true, "Draw progress bar smoothly")
                .setLanguageKey(LANG_PREFIX + CATEGORY_RENDERING + ".smoothProgressbar").getBoolean();

        textCursor = config
                .get(
                        CATEGORY_RENDERING,
                        "textCursor",
                        "underscore",
                        "Select: underscore, vertical",
                        new String[] { "underscore", "vertical" })
                .setLanguageKey(LANG_PREFIX + CATEGORY_RENDERING + ".textCursor").getString();

        // === Keyboard ===

        escRestoreLastText = config
                .get(
                        CATEGORY_KEYBOARD,
                        "escRestoreLastText",
                        false,
                        "Whether to restore last text if esc key is pressed in the text field")
                .setLanguageKey(LANG_PREFIX + CATEGORY_KEYBOARD + ".escRestoreLastText").getBoolean();

        closeWindowsAtOnce = config
                .get(CATEGORY_KEYBOARD, "closeWindowsAtOnce", false, "Whether to close all the opened windows at once")
                .setLanguageKey(LANG_PREFIX + CATEGORY_KEYBOARD + ".closeWindowsAtOnce").getBoolean();

        // === Localization ===

        Property property = config.get(
                CATEGORY_LOCALIZATION,
                "locale",
                Locale.getDefault().toLanguageTag(),
                "Locale to use to display GUI elements. Primarily used to display numbers in your regional format.")
                .setLanguageKey(LANG_PREFIX + CATEGORY_LOCALIZATION + ".locale");

        Locale newLocale = Locale.forLanguageTag(property.getString());
        if (NumberFormat.getNumberInstance(newLocale) instanceof DecimalFormat) {
            // If we can make sense of this locale, use it.
            locale = newLocale;
        } else {
            // Otherwise reset the config value.
            property.set(locale.toLanguageTag());
        }

        // === Json ===

        useJson = config
                .get(
                        CATEGORY_JSON,
                        "useJson",
                        false,
                        "Whether to enable Json. Enabling this will increase loading time.")
                .setLanguageKey(LANG_PREFIX + CATEGORY_JSON + ".useJson").getBoolean();

        // === Debug ===

        debug = config.get(CATEGORY_DEBUG, "debug", false, "Enable Debug information display")
                .setLanguageKey(LANG_PREFIX + CATEGORY_DEBUG + ".debug").getBoolean();

        forceEnableDebugBlock = config
                .get(CATEGORY_DEBUG, "forceEnableDebugBlock", false, "Add debug block even in non-dev env")
                .setLanguageKey(LANG_PREFIX + CATEGORY_DEBUG + ".forceEnableDebugBlock").setRequiresMcRestart(true)
                .getBoolean();

        if (config.hasChanged()) {
            config.save();
        }
    }
}
