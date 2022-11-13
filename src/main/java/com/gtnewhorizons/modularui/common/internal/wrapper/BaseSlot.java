package com.gtnewhorizons.modularui.common.internal.wrapper;

import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.forge.SlotItemHandler;
import com.gtnewhorizons.modularui.api.widget.ISyncedWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import java.util.function.Predicate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BaseSlot extends SlotItemHandler {

    protected final boolean phantom;
    protected boolean canInsert = true, canTake = true;

    private ISyncedWidget parentWidget;

    protected boolean enabled = true;
    // lower priority means it gets targeted first
    // hotbar 20, player inventory 40, machine input 0
    private int shiftClickPriority = 0;
    private boolean ignoreStackSizeLimit = false;
    private Runnable changeListener;
    private Predicate<ItemStack> filter;
    private ItemStack cachedItem = null;
    private boolean needsSyncing;

    private static final IItemHandlerModifiable EMPTY = new ItemStackHandler();

    public static BaseSlot phantom() {
        return phantom(new ItemStackHandler(), 0);
    }

    public static BaseSlot phantom(IItemHandlerModifiable handler, int index) {
        return new BaseSlot(handler, index, true);
    }

    public static BaseSlot empty() {
        return new BaseSlot(EMPTY, 0, true);
    }

    public BaseSlot(IItemHandlerModifiable inventory, int index) {
        this(inventory, index, false);
    }

    public BaseSlot(IItemHandlerModifiable inventory, int index, boolean phantom) {
        super(inventory, index, 0, 0);
        this.phantom = phantom;
        //        if (inventory instanceof PlayerMainInvWrapper) {
        //            setShiftClickPriority(index > 8 ? 40 : 20);
        //        }
        if (this.phantom) {
            this.shiftClickPriority += 10;
        }
    }

    public BaseSlot setShiftClickPriority(int shiftClickPriority) {
        this.shiftClickPriority = shiftClickPriority;
        return this;
    }

    public BaseSlot disableShiftInsert() {
        return setShiftClickPriority(Integer.MIN_VALUE);
    }

    public BaseSlot setAccess(boolean canTake, boolean canInsert) {
        this.canTake = canTake;
        this.canInsert = canInsert;
        return this;
    }

    public BaseSlot setIgnoreStackSizeLimit(boolean ignoreStackSizeLimit) {
        this.ignoreStackSizeLimit = ignoreStackSizeLimit;
        return this;
    }

    public BaseSlot setParentWidget(ISyncedWidget parentWidget) {
        this.parentWidget = parentWidget;
        return this;
    }

    public ISyncedWidget getParentWidget() {
        return parentWidget;
    }

    @Override
    public boolean isItemValid(@NotNull ItemStack stack) {
        return !this.phantom && isItemValidPhantom(stack);
    }

    /**
     * Override this instead of {@link #isItemValid} if you want to restrict shift insert.
     */
    public boolean isItemValidPhantom(ItemStack stack) {
        return this.canInsert
                && (filter == null || filter.test(stack))
                && getItemHandler().isItemValid(getSlotIndex(), stack);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return !this.phantom && canTake && isEnabled() && super.canTakeStack(playerIn);
    }

    /**
     * If this slot is disabled, player cannot interact with it.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * This is marked as client only, use {@link #isEnabled} instead.
     */
    @Override
    public boolean func_111238_b() {
        return isEnabled();
    }

    public boolean isCanInsert() {
        return canInsert;
    }

    public boolean isPhantom() {
        return phantom;
    }

    public boolean isIgnoreStackSizeLimit() {
        return ignoreStackSizeLimit;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getShiftClickPriority() {
        return shiftClickPriority;
    }

    public void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
    }

    public void setFilter(Predicate<ItemStack> filter) {
        this.filter = filter;
    }

    @Override
    public void onSlotChanged() {
        if (this.cachedItem != null && ItemStack.areItemStacksEqual(this.cachedItem, getStack())) {
            return;
        }
        if (getStack() != null) {
            this.cachedItem = getStack().copy();
        }
        this.needsSyncing = true;
        if (this.changeListener != null) {
            this.changeListener.run();
        }
        if (parentWidget == null) {
            throw new IllegalStateException("BaseSlot does not have parent widget to mark for update!");
        }
        this.parentWidget.markForUpdate();
    }

    public boolean isNeedsSyncing() {
        return needsSyncing;
    }

    public void resetNeedsSyncing() {
        this.needsSyncing = false;
    }

    // handle background by widgets
    @Override
    public ResourceLocation getBackgroundIconTexture() {
        return null;
    }

    //    @Nullable
    //    @Override
    //    public String getSlotTexture() {
    //        return null;
    //    }
    //
    //    @Nullable
    //    @Override
    //    public TextureAtlasSprite getBackgroundSprite() {
    //        return null;
    //    }

    public void incrementStackCount(int amount) {
        ItemStack stack = getStack();
        if (stack == null) {
            return;
        }
        int oldAmount = stack.stackSize;
        if (amount < 0) {
            amount = Math.max(0, oldAmount + amount);
        } else {
            if (parentWidget instanceof SlotWidget && !((SlotWidget) parentWidget).canControlAmount()) {
                amount = 1;
            } else {
                if (Integer.MAX_VALUE - amount < oldAmount) {
                    amount = Integer.MAX_VALUE;
                } else {
                    int maxSize = getItemHandler().getSlotLimit(getSlotIndex());
                    if (!isIgnoreStackSizeLimit() && stack.getMaxStackSize() < maxSize) {
                        maxSize = stack.getMaxStackSize();
                    }
                    amount = Math.min(oldAmount + amount, maxSize);
                }
            }
        }
        if (oldAmount != amount) {
            stack.stackSize = amount;
            if (stack.stackSize < 1) {
                putStack(null);
            } else {
                onSlotChanged();
            }
        }
    }
}
