package com.gtnewhorizons.modularui.api.drawable;

import com.gtnewhorizons.modularui.api.GlStateManager;

@SuppressWarnings("unused")
public class RotatedDrawable implements IDrawable {

    private final IDrawable drawable;
    private float rotation;

    public RotatedDrawable(IDrawable drawable) {
        this.drawable = drawable;
    }

    /**
     * @param rotation 0° - 360°, counter-clockwise
     */
    public RotatedDrawable setRotationDegree(float rotation) {
        this.rotation = rotation;
        return this;
    }

    /**
     * @param rotation 0 rad - 2π rad, counter-clockwise
     */
    public RotatedDrawable setRotationRadian(float rotation) {
        return setRotationDegree(180f / (float) Math.PI * rotation);
    }

    /**
     * @param rotation 0 rad - 2π rad, counter-clockwise
     */
    public RotatedDrawable setRotationRadian(double rotation) {
        return setRotationRadian((float) rotation);
    }

    @Override
    public void applyThemeColor(int color) {
        drawable.applyThemeColor(color);
    }

    @Override
    public void draw(float x, float y, float width, float height, float partialTicks) {
        GlStateManager.pushMatrix();
        drawable.rotate(rotation, width, height);
        drawable.draw(x, y, width, height, partialTicks);
        GlStateManager.popMatrix();
    }
}
