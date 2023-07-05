package com.gtnewhorizons.modularui.api.drawable.shapes;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;

import org.jetbrains.annotations.Contract;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.math.Color;

public class Circle implements IDrawable {

    public static final double PI2 = Math.PI * 2;

    private int colorInner, colorOuter, segments;

    public Circle() {
        this.colorInner = 0;
        this.colorOuter = 0;
        this.segments = 40;
    }

    @Contract("_ -> this")
    public Circle setColorInner(int colorInner) {
        this.colorInner = colorInner;
        return this;
    }

    public Circle setColorOuter(int colorOuter) {
        this.colorOuter = colorOuter;
        return this;
    }

    public Circle setColor(int inner, int outer) {
        this.colorInner = inner;
        this.colorOuter = outer;
        return this;
    }

    public Circle setSegments(int segments) {
        this.segments = segments;
        return this;
    }

    @Override
    public void applyThemeColor(int color) {
        if (colorInner == 0 && colorOuter == 0) {
            IDrawable.super.applyThemeColor(color == 0 ? 0xFFFFFFFF : color);
        }
    }

    @Override
    public void draw(float x0, float y0, float width, float height, float partialTicks) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.instance;
        float x_2 = x0 + width / 2, y_2 = y0 + height / 2;
        tessellator.startDrawing(GL11.GL_TRIANGLE_FAN);
        tessellator.setColorRGBA(
                Color.getRed(colorInner),
                Color.getGreen(colorInner),
                Color.getBlue(colorInner),
                Color.getAlpha(colorInner));
        tessellator.addVertex(x_2, y_2, 0.0f);
        float incr = (float) (PI2 / segments);
        for (int i = 0; i <= segments; i++) {
            float angle = incr * i;
            float x = (float) (Math.sin(angle) * (width / 2) + x_2);
            float y = (float) (Math.cos(angle) * (height / 2) + y_2);
            tessellator.startDrawing(GL11.GL_TRIANGLE_FAN);
            tessellator.setColorRGBA(
                    Color.getRed(colorOuter),
                    Color.getGreen(colorOuter),
                    Color.getBlue(colorOuter),
                    Color.getAlpha(colorOuter));
            tessellator.addVertex(x, y, 0.0f);
        }
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
