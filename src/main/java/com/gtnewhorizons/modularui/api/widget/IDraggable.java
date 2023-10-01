package com.gtnewhorizons.modularui.api.widget;

import java.awt.Rectangle;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface IDraggable {

    /**
     * Get's called from the cursor Usually you just call {@link Widget#drawInternal(float)}
     *
     * @param partialTicks difference from last from
     */
    void renderMovingState(float partialTicks);

    /**
     * @param button the mouse button that's holding down
     * @return false if the action should be canceled
     */
    boolean onDragStart(int button);

    /**
     * The dragging has ended and getState == IDLE
     *
     * @param successful is false if this returned to it's old position
     */
    void onDragEnd(boolean successful);

    void onDrag(int mouseButton, long timeSinceLastClick);

    /**
     * Gets called when the mouse is released
     *
     * @param widget     current top most widget below the mouse
     * @param isInBounds if the mouse is in the gui bounds
     * @return if the location is valid
     */
    default boolean canDropHere(@Nullable Widget widget, boolean isInBounds) {
        return isInBounds;
    }

    /**
     * @return the size and pos during move
     */
    @Nullable
    Rectangle getArea();

    default boolean shouldRenderChildren() {
        return true;
    }

    boolean isMoving();

    void setMoving(boolean moving);
}
