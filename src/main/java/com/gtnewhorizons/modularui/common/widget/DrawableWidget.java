package com.gtnewhorizons.modularui.common.widget;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.widget.Widget;

/**
 * Uses {@link IDrawable} to draw widget.
 */
public class DrawableWidget extends Widget {

    @NotNull
    private Supplier<IDrawable> drawable = () -> IDrawable.EMPTY;

    @Override
    public void readJson(JsonObject json, String type) {
        super.readJson(json, type);
        if (type.equals("image")) {
            setDrawable(UITexture.ofJson(json));
        }
    }

    @Override
    public void onScreenUpdate() {
        if (drawable.get() != null) {
            drawable.get().tick();
        }
    }

    @Override
    public void draw(float partialTicks) {
        if (drawable.get() != null) {
            GlStateManager.pushMatrix();
            // so that item z levels are properly ordered
            // todo: probably need better solution so that more windows can work
            GlStateManager.translate(0, 0, 150 * getWindowLayer());
            drawable.get().draw(Pos2d.ZERO, getSize(), partialTicks);
            GlStateManager.popMatrix();
        }
    }

    @Nullable
    public IDrawable getDrawable() {
        return drawable.get();
    }

    public DrawableWidget setDrawable(@Nullable IDrawable drawable) {
        if (drawable instanceof Text) {
            ModularUI.logger.warn("Please use TextWidget for Text");
        }
        this.drawable = () -> drawable;
        return this;
    }

    public DrawableWidget setDrawable(@NotNull Supplier<IDrawable> drawable) {
        this.drawable = drawable;
        return this;
    }
}
