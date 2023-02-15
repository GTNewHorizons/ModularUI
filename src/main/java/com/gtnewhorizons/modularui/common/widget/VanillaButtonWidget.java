package com.gtnewhorizons.modularui.common.widget;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.TextRenderer;
import com.gtnewhorizons.modularui.api.math.Alignment;

/**
 * {@link net.minecraft.client.gui.GuiButton}-alike button widget.
 */
public class VanillaButtonWidget extends ButtonWidget {

    private String displayString;
    private Supplier<Boolean> clickableGetter;
    private final TextRenderer textRenderer = new TextRenderer();
    private IDrawable[] normalBackground = new IDrawable[] { ModularUITextures.VANILLA_BUTTON_NORMAL };
    private IDrawable[] hoveredBackground = new IDrawable[] { ModularUITextures.VANILLA_BUTTON_HOVERED };
    private IDrawable[] disabledBackground = new IDrawable[] { ModularUITextures.VANILLA_BUTTON_DISABLED };

    @Override
    public @Nullable IDrawable[] getBackground() {
        if (!isClickable()) return disabledBackground;
        if (isHovering()) return hoveredBackground;
        return normalBackground;
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (!isClickable()) return ClickResult.REJECT;
        return super.onClick(buttonId, doubleClick);
    }

    @Override
    public void draw(float partialTicks) {
        if (displayString != null) {
            textRenderer.setPos(0, 0);
            textRenderer.setShadow(true);
            textRenderer.setAlignment(Alignment.Center, size.width, size.height);
            textRenderer.setColor(!isClickable() ? 0xa0a0a0 : isHovering() ? 0xffffa0 : 0xffffff);
            textRenderer.draw(displayString);
        }
    }

    public boolean isClickable() {
        return clickableGetter != null ? clickableGetter.get() : true;
    }

    public VanillaButtonWidget setDisplayString(String displayString) {
        this.displayString = displayString;
        return this;
    }

    public VanillaButtonWidget setClickableGetter(Supplier<Boolean> clickableGetter) {
        this.clickableGetter = clickableGetter;
        return this;
    }

    public VanillaButtonWidget setBackground(IDrawable[] normalBackground, IDrawable[] hoveredBackground,
            IDrawable[] disabledBackground) {
        this.normalBackground = normalBackground;
        this.hoveredBackground = hoveredBackground;
        this.disabledBackground = disabledBackground;
        return this;
    }

    public VanillaButtonWidget setOverlay(IDrawable normalOverlay, IDrawable hoveredOverlay,
            IDrawable disabledOverlay) {
        return setBackground(
                new IDrawable[] { ModularUITextures.VANILLA_BUTTON_NORMAL, normalOverlay },
                new IDrawable[] { ModularUITextures.VANILLA_BUTTON_HOVERED, hoveredOverlay },
                new IDrawable[] { ModularUITextures.VANILLA_BUTTON_DISABLED, disabledOverlay });
    }

    public VanillaButtonWidget setOverlay(IDrawable overlay) {
        return setOverlay(overlay, overlay, overlay);
    }
}
