package com.gtnewhorizons.modularui.common.internal.wrapper;

import static com.gtnewhorizons.modularui.ModularUI.isDevEnv;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

@SuppressWarnings("JavaReflectionMemberAccess")
public abstract class GuiContainerAccessor extends GuiContainer {

    private static final Field fieldTheSlot;
    private static final Field fieldClickedSlot;
    private static final Field fieldDraggedStack;
    private static final Field fieldIsRightMouseClick;
    private static final Field fieldDragSplittingLimit;
    private static final Method methodUpdateDragSplitting;
    private static final Field fieldDragSplittingRemnant;
    private static final Field fieldReturningStack;
    private static final Field fieldReturningStackDestSlot;
    private static final Field fieldTouchUpX;
    private static final Field fieldTouchUpY;
    private static final Field fieldReturningStackTime;

    static {
        final Class<?> gc = GuiContainer.class;
        try {
            fieldTheSlot = isDevEnv ? gc.getDeclaredField("theSlot") : gc.getDeclaredField("field_147006_u");
            fieldTheSlot.setAccessible(true);
            fieldClickedSlot = isDevEnv ? gc.getDeclaredField("clickedSlot") : gc.getDeclaredField("field_147005_v");
            fieldClickedSlot.setAccessible(true);
            fieldDraggedStack = isDevEnv ? gc.getDeclaredField("draggedStack") : gc.getDeclaredField("field_147012_x");
            fieldDraggedStack.setAccessible(true);
            fieldIsRightMouseClick =
                    isDevEnv ? gc.getDeclaredField("isRightMouseClick") : gc.getDeclaredField("field_147004_w");
            fieldIsRightMouseClick.setAccessible(true);
            fieldDragSplittingLimit = gc.getDeclaredField("field_146987_F");
            fieldDragSplittingLimit.setAccessible(true);
            methodUpdateDragSplitting = gc.getDeclaredMethod("func_146980_g");
            methodUpdateDragSplitting.setAccessible(true);
            fieldDragSplittingRemnant = gc.getDeclaredField("field_146996_I");
            fieldDragSplittingRemnant.setAccessible(true);
            fieldReturningStack =
                    isDevEnv ? gc.getDeclaredField("returningStack") : gc.getDeclaredField("field_146991_C");
            fieldReturningStack.setAccessible(true);
            fieldReturningStackDestSlot =
                    isDevEnv ? gc.getDeclaredField("returningStackDestSlot") : gc.getDeclaredField("field_146989_A");
            fieldReturningStackDestSlot.setAccessible(true);
            fieldTouchUpX = gc.getDeclaredField("field_147011_y");
            fieldTouchUpX.setAccessible(true);
            fieldTouchUpY = gc.getDeclaredField("field_147010_z");
            fieldTouchUpY.setAccessible(true);
            fieldReturningStackTime =
                    isDevEnv ? gc.getDeclaredField("returningStackTime") : gc.getDeclaredField("field_146990_B");
            fieldReturningStackTime.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public void setHoveredSlot(Slot slot) {
        try {
            fieldTheSlot.set(this, slot);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Slot getClickedSlot() {
        try {
            return (Slot) fieldClickedSlot.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public ItemStack getDraggedStack() {
        try {
            return (ItemStack) fieldDraggedStack.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean getIsRightMouseClick() {
        try {
            return (boolean) fieldIsRightMouseClick.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public int getDragSplittingLimit() {
        try {
            return (int) fieldDragSplittingLimit.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void invokeUpdateDragSplitting() {
        try {
            methodUpdateDragSplitting.invoke(this);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Set<Slot> getDragSplittingSlots() {
        return field_147008_s;
    }

    public boolean isDragSplittingInternal() {
        return field_147007_t;
    }

    public int getDragSplittingRemnant() {
        try {
            return (int) fieldDragSplittingRemnant.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public ItemStack getReturningStack() {
        try {
            return (ItemStack) fieldReturningStack.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setReturningStack(ItemStack stack) {
        try {
            fieldReturningStack.set(this, stack);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Slot getReturningStackDestSlot() {
        try {
            return (Slot) fieldReturningStackDestSlot.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public int getTouchUpX() {
        try {
            return (int) fieldTouchUpX.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public int getTouchUpY() {
        try {
            return (int) fieldTouchUpY.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public long getReturningStackTime() {
        try {
            return (long) fieldReturningStackTime.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public GuiContainerAccessor(Container p_i1072_1_) {
        super(p_i1072_1_);
    }
}
