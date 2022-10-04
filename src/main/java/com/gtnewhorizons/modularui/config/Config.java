package com.gtnewhorizons.modularui.config;

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

    public static boolean debug = false;

    public static final String CATEGORY_ANIMATIONS = "animations";
    public static final String CATEGORY_DEBUG = "debug";

    public static void init(File configFile) {
        config = new Configuration(configFile);
        syncConfig();
    }

    public static void syncConfig() {
        config.setCategoryComment(CATEGORY_ANIMATIONS, "Animations");
        config.setCategoryComment(CATEGORY_DEBUG, "Debug");

        openCloseDurationMs = config.get(
                        CATEGORY_ANIMATIONS,
                        "openCloseDurationMs",
                        200,
                        "How many milliseconds will it take to draw open/close animation",
                        0,
                        3000)
                .getInt();

        openCloseFade = config.get(
                        CATEGORY_ANIMATIONS,
                        "openCloseFade",
                        false,
                        "Whether to draw fade in/out animation on GUI open/close")
                .getBoolean();

        openCloseScale = config.get(
                        CATEGORY_ANIMATIONS,
                        "openCloseScale",
                        true,
                        "Whether to draw scale in/out animation on GUI open/close")
                .getBoolean();

        openCloseTranslateFromBottom = config.get(
                        CATEGORY_ANIMATIONS,
                        "openCloseTranslateFromBottom",
                        true,
                        "Whether to draw GUI coming out of / going out to the bottom of the screen on GUI open/close")
                .getBoolean();

        openCloseRotateFast = config.get(
                        CATEGORY_ANIMATIONS,
                        "openCloseRotateFast",
                        false,
                        "Whether to draw GUI rotating fast on GUI open/close")
                .getBoolean();

        smoothProgressbar = config.get(CATEGORY_ANIMATIONS, "smoothProgressbar", true, "Draw progress bar smoothly")
                .getBoolean();

        debug = config.get(CATEGORY_DEBUG, "debug", false, "Enable Debug information")
                .getBoolean();

        if (config.hasChanged()) {
            config.save();
        }
    }
}
