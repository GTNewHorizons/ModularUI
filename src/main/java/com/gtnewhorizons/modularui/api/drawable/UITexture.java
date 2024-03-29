package com.gtnewhorizons.modularui.api.drawable;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.google.gson.JsonObject;
import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.math.GuiArea;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.common.internal.JsonHelper;

public class UITexture implements IDrawable {

    public static final Map<ResourceLocation, UITexture> JSON_TEXTURES = new HashMap<>();

    public static final UITexture DEFAULT = fullImage("gui/options_background");

    public final ResourceLocation location;
    public final float u0, v0, u1, v1;

    /**
     * Creates a drawable texture
     *
     * @param location location of the texture
     * @param u0       x offset of the image (0-1)
     * @param v0       y offset of the image (0-1)
     * @param u1       x end offset of the image (0-1)
     * @param v1       y end offset of the image (0-1)
     */
    public UITexture(ResourceLocation location, float u0, float v0, float u1, float v1) {
        if (!location.getResourcePath().endsWith(".png")) {
            location = new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".png");
        }
        if (!location.getResourcePath().startsWith("textures/")) {
            location = new ResourceLocation(location.getResourceDomain(), "textures/" + location.getResourcePath());
        }
        this.location = location;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
    }

    public static UITexture fullImage(ResourceLocation location) {
        return new UITexture(location, 0, 0, 1, 1);
    }

    public static UITexture fullImage(String location) {
        return fullImage(new ResourceLocation(location));
    }

    public static UITexture fullImage(String mod, String location) {
        return fullImage(new ResourceLocation(mod, location));
    }

    public static UITexture partly(ResourceLocation location, int imageWidth, int imageHeight, int u0, int v0, int u1,
            int v1) {
        return new UITexture(
                location,
                u0 / (float) imageWidth,
                v0 / (float) imageHeight,
                u1 / (float) imageWidth,
                v1 / (float) imageHeight);
    }

    public static UITexture partly(String location, int imageWidth, int imageHeight, int u0, int v0, int u1, int v1) {
        return partly(new ResourceLocation(location), imageWidth, imageHeight, u0, v0, u1, v1);
    }

    public static UITexture partly(String domain, String location, int imageWidth, int imageHeight, int u0, int v0,
            int u1, int v1) {
        return partly(new ResourceLocation(domain, location), imageWidth, imageHeight, u0, v0, u1, v1);
    }

    public UITexture getSubArea(GuiArea bounds) {
        return getSubArea(bounds.x0, bounds.y0, bounds.x1, bounds.y1);
    }

    /**
     * Returns a texture with a sub area relative to this area texture
     *
     * @param uStart x offset of the image (0-1)
     * @param vStart y offset of the image (0-1)
     * @param uEnd   x end offset of the image (0-1)
     * @param vEnd   y end offset of the image (0-1)
     * @return relative sub area
     */
    public UITexture getSubArea(float uStart, float vStart, float uEnd, float vEnd) {
        return new UITexture(location, calcU(uStart), calcV(vStart), calcU(uEnd), calcV(vEnd));
    }

    public UITexture getFlipped(boolean horizontal, boolean vertical) {
        return getSubArea(horizontal ? 1 : 0, vertical ? 1 : 0, horizontal ? 0 : 1, vertical ? 0 : 1);
    }

    public UITexture exposeToJson() {
        if (JSON_TEXTURES.containsKey(location)) {
            UITexture texture = JSON_TEXTURES.get(location);
            ModularUI.logger.error(
                    "{} '{}' is already exposed to json with uv {}, {}, {}, {}!",
                    texture.getClass().getSimpleName(),
                    location,
                    texture.u0,
                    texture.v0,
                    texture.u1,
                    texture.v1);
        } else {
            JSON_TEXTURES.put(location, this);
        }
        return this;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    protected final float calcU(float uNew) {
        return (u1 - u0) * uNew + u0;
    }

    protected final float calcV(float vNew) {
        return (v1 - v0) * vNew + v0;
    }

    @Override
    public void draw(float x, float y, float width, float height, float partialTicks) {
        draw(x, y, width, height);
    }

    public void draw(float x, float y, float width, float height) {
        draw(location, x, y, width, height, u0, v0, u1, v1);
    }

    public void drawSubArea(float x, float y, float width, float height, float uStart, float vStart, float uEnd,
            float vEnd) {
        draw(location, x, y, width, height, calcU(uStart), calcV(vStart), calcU(uEnd), calcV(vEnd));
    }

    public static void draw(ResourceLocation location, float x0, float y0, float width, float height, float u0,
            float v0, float u1, float v1) {
        float x1 = x0 + width, y1 = y0 + height;
        Minecraft.getMinecraft().renderEngine.bindTexture(location);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        tessellator.setTextureUV(u0, v1);
        tessellator.addVertex(x0, y1, 0.0f);
        tessellator.setTextureUV(u1, v1);
        tessellator.addVertex(x1, y1, 0.0f);
        tessellator.setTextureUV(u1, v0);
        tessellator.addVertex(x1, y0, 0.0f);
        tessellator.setTextureUV(u0, v0);
        tessellator.addVertex(x0, y0, 0.0f);
        tessellator.draw();
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    public static UITexture ofJson(JsonObject json) {
        if (!json.has("src")) {
            return DEFAULT;
        }
        ResourceLocation rl = new ResourceLocation(json.get("src").getAsString());
        if (JSON_TEXTURES.containsKey(rl)) {
            return JSON_TEXTURES.get(rl);
        }
        float u0 = JsonHelper.getFloat(json, 0, "u", "u0"), v0 = JsonHelper.getFloat(json, 0, "v", "v0");
        float u1 = JsonHelper.getFloat(json, 1, "u1"), v1 = JsonHelper.getFloat(json, 1, "v1");
        Size imageSize = JsonHelper.getElement(json, Size.ZERO, Size::ofJson, "imageSize");
        int borderWidth = JsonHelper.getInt(json, -1, "borderWidth");
        if (imageSize.width > 0 && imageSize.height > 0 && borderWidth >= 0) {
            return AdaptableUITexture.of(rl, imageSize.width, imageSize.height, borderWidth).getSubArea(u0, v0, u1, v1);
        }
        return new UITexture(rl, u0, v0, u1, v1);
    }
}
