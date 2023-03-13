package com.gtnewhorizons.modularui.api.forge;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * Exposes the player inventory WITHOUT the armor inventory as IItemHandler. Also takes care of inserting/extracting
 * having the same logic as picking up items.
 */
public class PlayerMainInvWrapper extends RangedWrapper {

    private final InventoryPlayer inventoryPlayer;

    public PlayerMainInvWrapper(InventoryPlayer inv) {
        super(new InvWrapper(inv), 0, inv.mainInventory.length);
        inventoryPlayer = inv;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        ItemStack rest = super.insertItem(slot, stack, simulate);
        if (rest.stackSize != stack.stackSize) {
            // the stack in the slot changed, animate it
            ItemStack inSlot = getStackInSlot(slot);
            if (inSlot != null) {
                if (getInventoryPlayer().player.worldObj.isRemote) {
                    inSlot.animationsToGo = 5;
                } else if (getInventoryPlayer().player instanceof EntityPlayerMP) {
                    getInventoryPlayer().player.openContainer.detectAndSendChanges();
                }
            }
        }
        return rest;
    }

    public InventoryPlayer getInventoryPlayer() {
        return inventoryPlayer;
    }

    @Nullable
    @Override
    public IInventory getSourceInventory() {
        return inventoryPlayer;
    }
}
