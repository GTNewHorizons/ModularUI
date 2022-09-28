package com.gtnewhorizons.modularui.api.drawable;

import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.common.internal.Theme;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

public class TextRenderer {

    protected float maxWidth = -1, maxHeight = -1;
    protected int x = 0, y = 0;
    protected Alignment alignment = Alignment.TopLeft;
    protected float scale = 1f;
    protected boolean shadow = false;
    protected int color = Theme.INSTANCE.getText();
    protected boolean simulate;
    protected float lastWidth = 0, lastHeight = 0;

    public void setAlignment(Alignment alignment, float maxWidth) {
        setAlignment(alignment, maxWidth, -1);
    }

    public void setAlignment(Alignment alignment, float maxWidth, float maxHeight) {
        this.alignment = alignment;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setPos(Pos2d pos) {
        setPos(pos.x, pos.y);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setSimulate(boolean simulate) {
        this.simulate = simulate;
    }

    public void draw(String text) {
        draw(Collections.singletonList(text));
    }

    public void draw(List<String> lines) {
        drawMeasuredLines(measureLines(lines));
    }

    protected void drawMeasuredLines(List<Pair<String, Float>> measuredLines) {
        drawMeasuredLines(measuredLines, false);
    }

    protected void drawMeasuredLines(List<Pair<String, Float>> measuredLines, boolean hasSpaceAfterFirstLine) {
        float maxW = 0;
        int y0 = getStartY(measuredLines.size());
        boolean addedExtraSpace = false;
        final int EXTRA_SPACE = 2;
        for (Pair<String, Float> measuredLine : measuredLines) {
            int x0 = getStartX(measuredLine.getRight());
            maxW = Math.max(draw(measuredLine.getLeft(), x0, y0), maxW);
            y0 += getFontRenderer().FONT_HEIGHT * scale;
            if (hasSpaceAfterFirstLine && !addedExtraSpace && measuredLines.size() > 1) {
                y0 += EXTRA_SPACE;
                addedExtraSpace = true;
            }
        }
        this.lastWidth = maxWidth > 0 ? Math.min(maxW, maxWidth) : maxW;
        this.lastHeight = measuredLines.size() * getFontHeight() + (addedExtraSpace ? EXTRA_SPACE : 0);
        this.lastWidth = Math.max(0, this.lastWidth - scale);
        this.lastHeight = Math.max(0, this.lastHeight - scale);
    }

    public List<Pair<String, Float>> measureLines(List<String> lines) {
        List<Pair<String, Float>> measuredLines = new ArrayList<>();
        for (String line : lines) {
            for (String subLine : wrapLine(line)) {
                float width = getFontRenderer().getStringWidth(subLine) * scale;
                measuredLines.add(Pair.of(subLine, width));
            }
        }
        return measuredLines;
    }

    public List<String> wrapLine(String line) {
        return maxWidth > 0
                ? getFontRenderer().listFormattedStringToWidth(line, (int) (maxWidth / scale))
                : Collections.singletonList(line);
    }

    public boolean wouldFit(List<String> text) {
        if (maxHeight > 0 && maxHeight < text.size() * getFontHeight() - scale) {
            return false;
        }
        if (maxWidth > 0) {
            for (String line : text) {
                if (maxWidth < getFontRenderer().getStringWidth(line)) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getMaxWidth(List<String> lines) {
        if (lines.isEmpty()) {
            return 0;
        }
        List<Pair<String, Float>> measuredLines = measureLines(lines);
        float w = 0;
        for (Pair<String, Float> measuredLine : measuredLines) {
            w = Math.max(w, measuredLine.getRight());
        }
        return (int) Math.ceil(w);
    }

    protected int getStartY(int lines) {
        if (alignment.y >= 0 && maxHeight > 0) {
            float height = lines * getFontHeight() - scale;
            if (alignment.y > 0) {
                return (int) (y + maxHeight - height);
            } else {
                return (int) (y + (maxHeight - height) / 2f);
            }
        }
        return y;
    }

    protected int getStartX(float lineWidth) {
        if (maxWidth > 0 && alignment.x >= 0) {
            if (alignment.x > 0) {
                return (int) (x + maxWidth - lineWidth);
            } else {
                return (int) (x + (maxWidth - lineWidth) / 2f);
            }
        }
        return x;
    }

    protected float draw(String text, float x, float y) {
        if (simulate) {
            return getFontRenderer().getStringWidth(text);
        }
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 0f);
        GL11.glDisable(GL11.GL_LIGHTING);
        int width = getFontRenderer().drawString(text, (int) (x / scale), (int) (y / scale), color, shadow);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        return width * scale;
    }

    public float getFontHeight() {
        return getFontRenderer().FONT_HEIGHT * scale;
    }

    public float getLastHeight() {
        return lastHeight;
    }

    public float getLastWidth() {
        return lastWidth;
    }

    public Size getLastSize() {
        return new Size(lastWidth, lastHeight);
    }

    @SideOnly(Side.CLIENT)
    public static FontRenderer getFontRenderer() {
        return Minecraft.getMinecraft().fontRenderer;
    }
}
