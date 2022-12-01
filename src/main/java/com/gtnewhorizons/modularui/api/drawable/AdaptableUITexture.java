package com.gtnewhorizons.modularui.api.drawable;

import net.minecraft.util.ResourceLocation;

public class AdaptableUITexture extends UITexture {

    private final int imageWidth, imageHeight, borderWidthU, borderWidthV;

    public AdaptableUITexture(
            ResourceLocation location,
            float u0,
            float v0,
            float u1,
            float v1,
            int imageWidth,
            int imageHeight,
            int borderWidthU,
            int borderWidthV) {
        super(location, u0, v0, u1, v1);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.borderWidthU = borderWidthU;
        this.borderWidthV = borderWidthV;
    }

    public AdaptableUITexture(
            ResourceLocation location, int imageWidth, int imageHeight, int borderWidthU, int borderWidthV) {
        this(location, 0, 0, 1, 1, imageWidth, imageHeight, borderWidthU, borderWidthV);
    }

    public static AdaptableUITexture of(
            ResourceLocation location, int imageWidth, int imageHeight, int borderWidthU, int borderWidthV) {
        return new AdaptableUITexture(location, imageWidth, imageHeight, borderWidthU, borderWidthV);
    }

    public static AdaptableUITexture of(
            ResourceLocation location, int imageWidth, int imageHeight, int borderWidthPixel) {
        return new AdaptableUITexture(location, imageWidth, imageHeight, borderWidthPixel, borderWidthPixel);
    }

    public static AdaptableUITexture of(String location, int imageWidth, int imageHeight, int borderWidthPixel) {
        return new AdaptableUITexture(
                new ResourceLocation(location), imageWidth, imageHeight, borderWidthPixel, borderWidthPixel);
    }

    public static AdaptableUITexture of(
            String mod, String location, int imageWidth, int imageHeight, int borderWidthPixel) {
        return new AdaptableUITexture(
                new ResourceLocation(mod, location), imageWidth, imageHeight, borderWidthPixel, borderWidthPixel);
    }

    @Override
    public AdaptableUITexture getSubArea(float uStart, float vStart, float uEnd, float vEnd) {
        return new AdaptableUITexture(
                location,
                calcU(uStart),
                calcV(vStart),
                calcU(uEnd),
                calcV(vEnd),
                imageWidth,
                imageHeight,
                borderWidthU,
                borderWidthV);
    }

    @Override
    public AdaptableUITexture exposeToJson() {
        return (AdaptableUITexture) super.exposeToJson();
    }

    @Override
    public void draw(float x, float y, float width, float height) {
        if (width == imageWidth && height == imageHeight) {
            super.draw(x, y, width, height);
            return;
        }
        float borderU = borderWidthU * 1f / imageWidth;
        float borderV = borderWidthV * 1f / imageHeight;
        // draw corners
        draw(location, x, y, borderWidthU, borderWidthV, u0, v0, u0 + borderU, v0 + borderV); // x0 y0
        draw(
                location,
                x + width - borderWidthU,
                y,
                borderWidthU,
                borderWidthV,
                u1 - borderU,
                v0,
                u1,
                v0 + borderV); // x1 y0
        draw(
                location,
                x,
                y + height - borderWidthV,
                borderWidthU,
                borderWidthV,
                u0,
                v1 - borderV,
                u0 + borderU,
                v1); // x0 y1
        draw(
                location,
                x + width - borderWidthU,
                y + height - borderWidthV,
                borderWidthU,
                borderWidthV,
                u1 - borderU,
                v1 - borderV,
                u1,
                v1); // x1 y1
        // draw edges
        draw(
                location,
                x + borderWidthU,
                y,
                width - borderWidthU * 2,
                borderWidthV,
                u0 + borderU,
                v0,
                u1 - borderU,
                v0 + borderV); // top
        draw(
                location,
                x + borderWidthU,
                y + height - borderWidthV,
                width - borderWidthU * 2,
                borderWidthV,
                u0 + borderU,
                v1 - borderV,
                u1 - borderU,
                v1); // bottom
        draw(
                location,
                x,
                y + borderWidthV,
                borderWidthU,
                height - borderWidthV * 2,
                u0,
                v0 + borderV,
                u0 + borderU,
                v1 - borderV); // left
        draw(
                location,
                x + width - borderWidthU,
                y + borderWidthV,
                borderWidthU,
                height - borderWidthV * 2,
                u1 - borderU,
                v0 + borderV,
                u1,
                v1 - borderV); // left
        // draw body
        draw(
                location,
                x + borderWidthU,
                y + borderWidthV,
                width - borderWidthU * 2,
                height - borderWidthV * 2,
                u0 + borderU,
                v0 + borderV,
                u1 - borderU,
                v1 - borderV);
    }
}
