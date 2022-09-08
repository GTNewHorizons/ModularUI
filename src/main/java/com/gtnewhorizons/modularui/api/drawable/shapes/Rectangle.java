package com.gtnewhorizons.modularui.api.drawable.shapes;

import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.math.Color;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

public class Rectangle implements IDrawable {

    public static final double PI_2 = Math.PI / 2;

    private int cornerRadius, colorTL, colorTR, colorBL, colorBR, cornerSegments;

    public Rectangle() {
        this.cornerRadius = 0;
        this.colorTL = 0;
        this.colorTR = 0;
        this.colorBL = 0;
        this.colorBR = 0;
        this.cornerSegments = 6;
    }

    public Rectangle setCornerRadius(int cornerRadius) {
        this.cornerRadius = Math.max(0, cornerRadius);
        return this;
    }

    public Rectangle setColor(int colorTL, int colorTR, int colorBL, int colorBR) {
        this.colorTL = colorTL;
        this.colorTR = colorTR;
        this.colorBL = colorBL;
        this.colorBR = colorBR;
        return this;
    }

    public Rectangle setVerticalGradient(int colorTop, int colorBottom) {
        return setColor(colorTop, colorTop, colorBottom, colorBottom);
    }

    public Rectangle setHorizontalGradient(int colorLeft, int colorRight) {
        return setColor(colorLeft, colorRight, colorLeft, colorRight);
    }

    public Rectangle setColor(int color) {
        return setColor(color, color, color, color);
    }

    public Rectangle setCornerSegments(int cornerSegments) {
        this.cornerSegments = cornerSegments;
        return this;
    }

    @Override
    public void applyThemeColor(int color) {
        if (colorTL == 0 && colorBL == 0 && colorBR == 0 && colorTR == 0) {
            IDrawable.super.applyThemeColor(color == 0 ? 0xFFFFFFFF : color);
        }
    }

    @Override
    public void draw(float x0, float y0, float width, float height, float partialTicks) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.instance;
        float x1 = x0 + width, y1 = y0 + height;
        if (this.cornerRadius == 0) {
            tessellator.startDrawingQuads();

            tessellator.setColorRGBA_F(
                    Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL));
            tessellator.addVertex(x0, y0, 0.0f);
            tessellator.setColorRGBA_F(
                    Color.getRed(colorBL), Color.getGreen(colorBL), Color.getBlue(colorBL), Color.getAlpha(colorBL));
            tessellator.addVertex(x0, y1, 0.0f);
            tessellator.setColorRGBA_F(
                    Color.getRed(colorBR), Color.getGreen(colorBR), Color.getBlue(colorBR), Color.getAlpha(colorBR));
            tessellator.addVertex(x1, y1, 0.0f);
            tessellator.setColorRGBA_F(
                    Color.getRed(colorTR), Color.getGreen(colorTR), Color.getBlue(colorTR), Color.getAlpha(colorTR));
            tessellator.addVertex(x1, y0, 0.0f);
        } else {
            tessellator.startDrawing(GL11.GL_TRIANGLE_FAN);
            int color = Color.average(colorBL, colorBR, colorTR, colorTL);
            tessellator.setColorRGBA_F(
                    Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color));
            tessellator.addVertex(x0 + width / 2, y0 + height / 2, 0.0f);
            tessellator.setColorRGBA_F(
                    Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL));
            tessellator.addVertex(x0, y0 + cornerRadius, 0.0f);
            tessellator.setColorRGBA_F(
                    Color.getRed(colorBL), Color.getGreen(colorBL), Color.getBlue(colorBL), Color.getAlpha(colorBL));
            tessellator.addVertex(x0, y1 - cornerRadius, 0.0f);
            int n = cornerSegments;
            for (int i = 1; i <= n; i++) {
                float x = (float) (x0 + cornerRadius - Math.cos(PI_2 / n * i) * cornerRadius);
                float y = (float) (y1 - cornerRadius + Math.sin(PI_2 / n * i) * cornerRadius);
                tessellator.setColorRGBA_F(
                        Color.getRed(colorBL),
                        Color.getGreen(colorBL),
                        Color.getBlue(colorBL),
                        Color.getAlpha(colorBL));
                tessellator.addVertex(x, y, 0.0f);
            }
            tessellator.setColorRGBA_F(
                    Color.getRed(colorBR), Color.getGreen(colorBR), Color.getBlue(colorBR), Color.getAlpha(colorBR));
            tessellator.addVertex(x1 - cornerRadius, y1, 0.0f);
            for (int i = 1; i <= n; i++) {
                float x = (float) (x1 - cornerRadius + Math.sin(PI_2 / n * i) * cornerRadius);
                float y = (float) (y1 - cornerRadius + Math.cos(PI_2 / n * i) * cornerRadius);
                tessellator.setColorRGBA_F(
                        Color.getRed(colorBR),
                        Color.getGreen(colorBR),
                        Color.getBlue(colorBR),
                        Color.getAlpha(colorBR));
                tessellator.addVertex(x, y, 0.0f);
            }
            tessellator.setColorRGBA_F(
                    Color.getRed(colorTR), Color.getGreen(colorTR), Color.getBlue(colorTR), Color.getAlpha(colorTR));
            tessellator.addVertex(x1, y0 + cornerRadius, 0.0f);
            for (int i = 1; i <= n; i++) {
                float x = (float) (x1 - cornerRadius + Math.cos(PI_2 / n * i) * cornerRadius);
                float y = (float) (y0 + cornerRadius - Math.sin(PI_2 / n * i) * cornerRadius);
                tessellator.setColorRGBA_F(
                        Color.getRed(colorTR),
                        Color.getGreen(colorTR),
                        Color.getBlue(colorTR),
                        Color.getAlpha(colorTR));
                tessellator.addVertex(x, y, 0.0f);
            }
            tessellator.setColorRGBA_F(
                    Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL));
            tessellator.addVertex(x0 + cornerRadius, y0, 0.0f);
            for (int i = 1; i <= n; i++) {
                float x = (float) (x0 + cornerRadius - Math.sin(PI_2 / n * i) * cornerRadius);
                float y = (float) (y0 + cornerRadius - Math.cos(PI_2 / n * i) * cornerRadius);
                tessellator.setColorRGBA_F(
                        Color.getRed(colorTL),
                        Color.getGreen(colorTL),
                        Color.getBlue(colorTL),
                        Color.getAlpha(colorTL));
                tessellator.addVertex(x, y, 0.0f);
            }
            tessellator.setColorRGBA_F(
                    Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL));
            tessellator.addVertex(x0, y0 + cornerRadius, 0.0f);
        }
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}
