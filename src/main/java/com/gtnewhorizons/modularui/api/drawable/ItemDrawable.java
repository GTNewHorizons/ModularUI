package com.gtnewhorizons.modularui.api.drawable;

import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.api.Widget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Draws item. Can also be used for {@link Widget}
 */
public class ItemDrawable implements IDrawable {

    private ItemStack item = null;

    private static final RenderItem itemRenderer = new RenderItem();

    public ItemDrawable(@NotNull ItemStack item) {
        this.item = item;
    }

    @Override
    public void applyThemeColor(int color) {}

    @Override
    public void draw(float x, float y, float width, float height, float partialTicks) {
        if (item.getItem() == null) return;
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.scale(width / 16, height / 16, 1);
        itemRenderer.renderItemAndEffectIntoGUI(
                item.getItem().getFontRenderer(item), Minecraft.getMinecraft().getTextureManager(), item, (int) x, (int)
                        y);
        GlStateManager.disableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public DrawableWidget asWidget() {
        return (DrawableWidget) IDrawable.super.asWidget().setSize(16, 16);
    }

    public ItemDrawable setItem(@NotNull ItemStack item) {
        this.item = item;
        return this;
    }
}
