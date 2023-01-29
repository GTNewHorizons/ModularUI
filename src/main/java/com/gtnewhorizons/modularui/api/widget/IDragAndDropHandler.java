package com.gtnewhorizons.modularui.api.widget;

import net.minecraft.item.ItemStack;

/**
 * Implement this interface for your widget to handle drag-and-drop from NEI.
 */
public interface IDragAndDropHandler {

    /**
     * Implement your drag-and-drop behavior here. The held stack will be deleted if draggedStack.stackSize == 0.
     * 
     * @param draggedStack Item dragged from NEI
     * @param button       0 = left click, 1 = right click
     * @return True if success
     */
    boolean handleDragAndDrop(ItemStack draggedStack, int button);
}
