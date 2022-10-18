package com.gtnewhorizons.modularui.config;

import com.gtnewhorizons.modularui.ModularUI;
import java.io.File;
import net.minecraftforge.common.config.Configuration;

public class Config {

    public static Configuration config;

    public static int openCloseDurationMs = 200;
    public static boolean openCloseFade = false;
    public static boolean openCloseScale = true;
    public static boolean openCloseTranslateFromBottom = true;
    public static boolean openCloseRotateFast = false;

    public static boolean smoothProgressbar = true;
    public static String textCursor = "underscore";

    public static boolean escRestoreLastText = false;

    public static boolean debug = false;
    public static boolean forceEnableDebugBlock = false;

    public static final String CATEGORY_ANIMATIONS = "animations";
    public static final String CATEGORY_RENDERING = "rendering";
    public static final String CATEGORY_KEYBOARD = "keyboard";
    public static final String CATEGORY_DEBUG = "debug";

    private static final String LANG_PREFIX = ModularUI.MODID + ".config.";

    public static final String[] CATEGORIES = new String[] {
        CATEGORY_ANIMATIONS, CATEGORY_RENDERING, CATEGORY_KEYBOARD, CATEGORY_DEBUG,
    };

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
        config.setCategoryComment(CATEGORY_DEBUG, "Debug");
        config.setCategoryLanguageKey(CATEGORY_DEBUG, LANG_PREFIX + CATEGORY_DEBUG);

        // === Animations ===

        openCloseDurationMs = config.get(
                        CATEGORY_ANIMATIONS,
                        "openCloseDurationMs",
                        200,
                        "How many milliseconds will it take to draw open/close animation",
                        0,
                        3000)
                .setLanguageKey(LANG_PREFIX + CATEGORY_ANIMATIONS + ".openCloseDurationMs")
                .getInt();

        openCloseFade = config.get(
                        CATEGORY_ANIMATIONS,
                        "openCloseFade",
                        false,
                        "Whether to draw fade in/out animation on GUI open/close")
                .setLanguageKey(LANG_PREFIX + CATEGORY_ANIMATIONS + ".openCloseFade")
                .getBoolean();

        openCloseScale = config.get(
                        CATEGORY_ANIMATIONS,
                        "openCloseScale",
                        true,
                        "Whether to draw scale in/out animation on GUI open/close")
                .setLanguageKey(LANG_PREFIX + CATEGORY_ANIMATIONS + ".openCloseScale")
                .getBoolean();

        openCloseTranslateFromBottom = config.get(
                        CATEGORY_ANIMATIONS,
                        "openCloseTranslateFromBottom",
                        true,
                        "Whether to draw GUI coming out of / going out to the bottom of the screen on GUI open/close")
                .setLanguageKey(LANG_PREFIX + CATEGORY_ANIMATIONS + ".openCloseTranslateFromBottom")
                .getBoolean();

        openCloseRotateFast = config.get(
                        CATEGORY_ANIMATIONS,
                        "openCloseRotateFast",
                        false,
                        "Whether to draw GUI rotating fast on GUI open/close")
                .setLanguageKey(LANG_PREFIX + CATEGORY_ANIMATIONS + ".openCloseRotateFast")
                .getBoolean();

        // === Rendering ===

        smoothProgressbar = config.get(CATEGORY_RENDERING, "smoothProgressbar", true, "Draw progress bar smoothly")
                .setLanguageKey(LANG_PREFIX + CATEGORY_RENDERING + ".smoothProgressbar")
                .getBoolean();

        textCursor = config.get(
                        CATEGORY_RENDERING, "textCursor", "underscore", "Select: underscore, vertical", new String[] {
                            "underscore", "vertical"
                        })
                .setLanguageKey(LANG_PREFIX + CATEGORY_RENDERING + ".textCursor")
                .getString();

        // === Keyboard ===

        escRestoreLastText = config.get(
                        CATEGORY_KEYBOARD,
                        "escRestoreLastText",
                        false,
                        "Whether to restore last text if esc key is pressed in the text field")
                .setLanguageKey(LANG_PREFIX + CATEGORY_KEYBOARD + ".escRestoreLastText")
                .getBoolean();

        // === Debug ===

        debug = config.get(CATEGORY_DEBUG, "debug", false, "Enable Debug information display")
                .setLanguageKey(LANG_PREFIX + CATEGORY_DEBUG + ".debug")
                .getBoolean();

        forceEnableDebugBlock = config.get(
                        CATEGORY_DEBUG, "forceEnableDebugBlock", false, "Add debug block even in non-dev env")
                .setLanguageKey(LANG_PREFIX + CATEGORY_DEBUG + ".forceEnableDebugBlock")
                .setRequiresMcRestart(true)
                .getBoolean();

        if (config.hasChanged()) {
            config.save();
        }
    }
}
