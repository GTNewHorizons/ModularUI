package com.gtnewhorizons.modularui.api.screen;

import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.widget.IDraggable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import java.awt.*;
import org.jetbrains.annotations.Nullable;

public class DraggableWindowWrapper implements IDraggable {

    private final ModularWindow window;
    private final Pos2d relativeClickPos;
    private boolean moving;
    private final Rectangle area = new Rectangle();

    public DraggableWindowWrapper(ModularWindow window, Pos2d relativeClickPos) {
        this.window = window;
        this.relativeClickPos = relativeClickPos;
        this.area.setSize(window.getSize().width, window.getSize().height);
    }

    @Override
    public void renderMovingState(float delta) {
        Cursor cursor = window.getContext().getCursor();
        GlStateManager.pushMatrix();
        GlStateManager.translate(-window.getPos().x, -window.getPos().y, 0);
        GlStateManager.translate(cursor.getX() - relativeClickPos.x, cursor.getY() - relativeClickPos.y, 0);
        window.drawWidgets(delta, false);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean onDragStart(int button) {
        return button == 0;
    }

    @Override
    public void onDragEnd(boolean successful) {
        if (successful) {
            window.setPos(window.getContext().getCursor().getPos().subtract(relativeClickPos));
            window.markNeedsRebuild();
        }
    }

    @Override
    public void onDrag(int mouseButton, long timeSinceLastClick) {}

    @Override
    public boolean canDropHere(@Nullable Widget widget, boolean isInBounds) {
        return true;
    }

    @Override
    public @Nullable Rectangle getArea() {
        Pos2d cursor = window.getContext().getCursor().getPos();
        this.area.setLocation(cursor.x - relativeClickPos.x, cursor.y - relativeClickPos.y);
        return this.area;
    }

    @Override
    public boolean isMoving() {
        return moving;
    }

    @Override
    public void setMoving(boolean moving) {
        this.moving = moving;
        this.window.setEnabled(!moving);
    }
}
