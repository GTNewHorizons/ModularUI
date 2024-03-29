package com.gtnewhorizons.modularui.common.widget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.api.animation.Eases;
import com.gtnewhorizons.modularui.api.animation.Interpolator;
import com.gtnewhorizons.modularui.api.drawable.GuiHelper;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.Theme;
import com.gtnewhorizons.modularui.config.Config;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Tab-styled widget that can contain multiple widget children and toggle expanded/collapsed by clicking this.
 */
public class ExpandTab extends MultiChildWidget implements Interactable, IWidgetBuilder<ExpandTab> {

    private boolean expanded = false, animating = false, firstBuild = true;
    private Interpolator openAnimator;
    private Interpolator closeAnimator;
    protected Size expandedSize;
    protected Size normalSize;
    protected Pos2d expandedPos;
    protected Pos2d normalPos;
    private int animateDuration = Config.openCloseDurationMs;
    private float animateX, animateY, animateWidth, animateHeight;

    @Nullable
    private IDrawable[] normalTexture;

    private float ticktime;

    @Override
    public void onInit() {
        this.openAnimator = new Interpolator(0, 1, this.animateDuration, Eases.EaseQuadOut, value -> {
            float val = (float) value;
            this.animateX = (this.expandedPos.x - this.normalPos.x) * val + this.normalPos.x;
            this.animateY = (this.expandedPos.y - this.normalPos.y) * val + this.normalPos.y;
            this.animateWidth = (this.expandedSize.width - this.normalSize.width) * val + this.normalSize.width;
            this.animateHeight = (this.expandedSize.height - this.normalSize.height) * val + this.normalSize.height;
        }, val -> {
            this.animateX = this.expandedPos.x;
            this.animateY = this.expandedPos.y;
            this.animateWidth = this.expandedSize.width;
            this.animateHeight = this.expandedSize.height;
            this.animating = false;
        });
        this.closeAnimator = this.openAnimator.getReversed(this.animateDuration, Eases.EaseQuadIn);
        this.closeAnimator.setCallback(val -> {
            this.animateX = this.normalPos.x;
            this.animateY = this.normalPos.y;
            this.animateWidth = this.normalSize.width;
            this.animateHeight = this.normalSize.height;
            this.animating = false;
            for (Widget widget : getChildren()) {
                if (!shouldDrawChildWidgetWhenCollapsed(widget)) {
                    widget.setEnabled(false);
                }
            }
        });
        for (Widget widget : getChildren()) {
            if (!shouldDrawChildWidgetWhenCollapsed(widget)) {
                widget.setEnabled(false);
            }
        }

        if (isClient()) {
            FMLCommonHandler.instance().bus().register(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FMLCommonHandler.instance().bus().unregister(this);
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return new Size(20, 20);
    }

    @Override
    public void onRebuild() {
        if (firstBuild) {
            if (this.normalPos == null) {
                this.normalPos = getPos();
            }
            if (this.normalSize == null) {
                this.normalSize = getSize();
            }
            if (this.expandedPos == null) {
                this.expandedPos = this.normalPos;
            }
            if (this.expandedSize == null) {
                this.expandedSize = new Size(this.normalSize.width * 3, this.normalSize.height * 3);
            }
            this.animateX = getPos().x;
            this.animateY = getPos().y;
            this.animateWidth = getSize().width;
            this.animateHeight = getSize().height;
            this.firstBuild = false;
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        ticktime = event.renderTickTime;
    }

    @Override
    public void onFrameUpdate() {
        if (this.animating) {
            if (expanded) {
                this.openAnimator.update(ticktime);
            } else {
                this.closeAnimator.update(ticktime);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawBackground(float partialTicks) {
        IDrawable[] background = getBackground();
        if (background != null) {
            int themeColor = Theme.INSTANCE.getColor(getBackgroundColorKey());
            for (IDrawable drawable : background) {
                if (drawable != null) {
                    drawable.applyThemeColor(themeColor);
                    IDrawable.applyTintColor(getWindow().getGuiTint());
                    drawable.draw(
                            animateX - getPos().x,
                            animateY - getPos().y,
                            animateWidth,
                            animateHeight,
                            partialTicks);
                }
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        if (!isExpanded() && this.normalTexture != null) {
            for (IDrawable drawable : this.normalTexture) {
                if (drawable != null) {
                    drawable.applyThemeColor();
                    drawable.draw(Pos2d.ZERO, this.normalSize, partialTicks);
                }
            }
        }
    }

    @Override
    public void drawChildren(float partialTicks) {
        if (isExpanded() || animating) {
            Pos2d parentPos = getParent().getAbsolutePos();
            GlStateManager.pushMatrix();
            Pos2d relativePos = isExpanded() ? expandedPos : normalPos;
            GlStateManager.translate(animateX - relativePos.x, animateY - relativePos.y, 0);
            if (animating) {
                GuiHelper.useScissor(
                        (int) (parentPos.x + this.animateX),
                        (int) (parentPos.y + this.animateY),
                        (int) this.animateWidth,
                        (int) this.animateHeight,
                        () -> { super.drawChildren(partialTicks); });
            } else {
                super.drawChildren(partialTicks);
            }
            GlStateManager.popMatrix();
        } else {
            for (Widget child : getChildren()) {
                if (shouldDrawChildWidgetWhenCollapsed(child)) {
                    child.drawInternal(partialTicks);
                }
            }
        }
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (buttonId == 0) {
            setExpanded(!isExpanded());
            return ClickResult.ACCEPT;
        }
        return ClickResult.ACKNOWLEDGED;
    }

    public void setExpanded(boolean expanded) {
        if (this.expanded != expanded) {
            this.expanded = expanded;
            this.animating = true;

            if (isExpanded()) {
                for (Widget widget : getChildren()) {
                    widget.setEnabled(true);
                }
                openAnimator.forward();
                super.setSize(expandedSize);
                super.setPos(expandedPos);
            } else {
                closeAnimator.forward();
                super.setSize(normalSize);
                super.setPos(normalPos);
            }
            checkNeedsRebuild();
        }
    }

    public boolean isExpanded() {
        return expanded;
    }

    @Override
    public void addWidgetInternal(Widget widget) {
        addChild(widget);
    }

    public boolean shouldDrawChildWidgetWhenCollapsed(Widget child) {
        return false;
    }

    public ExpandTab setExpandedPos(int x, int y) {
        return setExpandedPos(new Pos2d(x, y));
    }

    public ExpandTab setExpandedPos(Pos2d expandedPos) {
        this.expandedPos = expandedPos;
        return this;
    }

    public ExpandTab setExpandedSize(int width, int height) {
        return setExpandedSize(new Size(width, height));
    }

    public ExpandTab setExpandedSize(Size expandedSize) {
        this.expandedSize = expandedSize;
        return this;
    }

    @Override
    public ExpandTab setSize(Size size) {
        super.setSize(size);
        this.normalSize = size;
        return this;
    }

    @Override
    public ExpandTab setPos(Pos2d relativePos) {
        super.setPos(relativePos);
        this.normalPos = relativePos;
        return this;
    }

    public ExpandTab setAnimateDuration(int animateDuration) {
        this.animateDuration = animateDuration;
        return this;
    }

    public ExpandTab setNormalTexture(IDrawable... normalTexture) {
        this.normalTexture = normalTexture;
        return this;
    }
}
