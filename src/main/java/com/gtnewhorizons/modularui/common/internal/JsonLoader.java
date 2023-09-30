package com.gtnewhorizons.modularui.common.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.forge.CraftingHelper;
import com.gtnewhorizons.modularui.config.Config;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class JsonLoader {

    public static final Map<ResourceLocation, JsonObject> GUIS = new HashMap<>();
    public static final JsonParser jsonParser = new JsonParser();
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void loadJson() {
        if (!Config.useJson) return;
        GUIS.clear();
        List<ModContainer> mods = Loader.instance().getActiveModList();
        mods.forEach((mod) -> {
            String id = mod.getModId();
            CraftingHelper
                    .findFiles(mod, String.format("assets/%s/guis", id), (path) -> Files.exists(path), (path, file) -> {
                        if (file.toString().endsWith(".json")) {
                            JsonObject json = tryExtractFromFile(file);
                            if (json != null) {
                                String fileStr = file.toString().replaceAll("\\\\", "/");
                                String guiName = fileStr.substring(fileStr.indexOf("guis/") + 5, fileStr.length() - 5);

                                ResourceLocation bookId = new ResourceLocation(id, guiName);
                                GUIS.put(bookId, json);
                            }
                        }
                        return true;
                    }, false, true);
        });
        ModularUI.logger.info("Loaded {} guis from json", GUIS.size());
    }

    public static JsonObject tryExtractFromFile(Path filePath) {
        try (InputStream fileStream = Files.newInputStream(filePath)) {
            InputStreamReader streamReader = new InputStreamReader(fileStream, StandardCharsets.UTF_8);
            return jsonParser.parse(streamReader).getAsJsonObject();
        } catch (IOException exception) {
            ModularUI.logger.error("Failed to read file on path {}", filePath, exception);
        } catch (JsonParseException exception) {
            ModularUI.logger.error("Failed to extract json from file", exception);
        } catch (Exception exception) {
            ModularUI.logger.error("Failed to extract json from file on path {}", filePath, exception);
        }

        return null;
    }
}
