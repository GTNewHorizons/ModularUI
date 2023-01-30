package com.gtnewhorizons.modularui.api.drawable;

import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.api.math.Size;

public class RepeatingDrawable implements IDrawable {

    private IDrawable drawable;
    private Size drawableSize;
    private int spaceX, spaceY;

    public RepeatingDrawable setDrawable(IDrawable drawable) {
        this.drawable = drawable;
        return this;
    }

    public RepeatingDrawable setDrawableSize(Size size) {
        this.drawableSize = size;
        return this;
    }

    public RepeatingDrawable setDrawableSize(int width, int height) {
        return setDrawableSize(new Size(width, height));
    }

    public RepeatingDrawable setSpaceX(int spaceX) {
        this.spaceX = spaceX;
        return this;
    }

    public RepeatingDrawable setSpaceY(int spaceY) {
        this.spaceY = spaceY;
        return this;
    }

    @Override
    public void draw(float x0, float y0, float width, float height, float partialTicks) {
        if (drawable == null) return;
        if (width <= 0 || height <= 0) return;
        if (drawableSize.width <= 0 || drawableSize.height <= 0) return;
        double[] translation = GlStateManager.getTranslation();

        GuiHelper
                .useScissor((int) (x0 + translation[0]), (int) (y0 + translation[1]), (int) width, (int) height, () -> {
                    float x = x0, y = y0;
                    final float x1 = x0 + width, y1 = y0 + height;
                    while (y < y1) {
                        while (x < x1) {
                            drawable.draw(x, y, drawableSize.width, drawableSize.height, partialTicks);
                            x += drawableSize.width + spaceX;
                        }
                        y += drawableSize.height + spaceY;
                        x = x0;
                    }
                });
    }
}
