package com.gtnewhorizons.modularui.common.widget;

import static com.gtnewhorizons.modularui.ModularUI.isGT5ULoaded;
import static com.gtnewhorizons.modularui.ModularUI.isNEILoaded;

import codechicken.nei.recipe.StackInfo;
import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.NumberFormat;
import com.gtnewhorizons.modularui.api.drawable.GuiHelper;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.drawable.TextRenderer;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.IDragAndDropHandler;
import com.gtnewhorizons.modularui.api.widget.IHasStackUnderMouse;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.Theme;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import gregtech.api.util.GT_Utility;
import gregtech.common.items.GT_FluidDisplayItem;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class FluidSlotWidget extends SyncedWidget implements Interactable, IDragAndDropHandler, IHasStackUnderMouse {

    public static final Size SIZE = new Size(18, 18);

    @Nullable
    private IDrawable overlayTexture;

    private final TextRenderer textRenderer = new TextRenderer();
    private final IFluidTank fluidTank;

    @Nullable
    private FluidStack lastStoredFluid;

    private FluidStack lastStoredPhantomFluid;
    private Pos2d contentOffset = new Pos2d(1, 1);
    private boolean alwaysShowFull = true;
    private boolean canDrainSlot = true;
    private boolean canFillSlot = true;
    private boolean phantom = false;
    private boolean controlsAmount = false;
    private boolean lastShift = false;
    private boolean playClickSound = true;

    private Consumer<Widget> onDragAndDropComplete;

    public FluidSlotWidget(IFluidTank fluidTank) {
        this.fluidTank = fluidTank;
        this.textRenderer.setColor(Color.WHITE.normal);
        this.textRenderer.setShadow(true);
    }

    public static FluidSlotWidget phantom(IFluidTank fluidTank, boolean controlsAmount) {
        FluidSlotWidget slot = new FluidSlotWidget(fluidTank);
        slot.phantom = true;
        slot.controlsAmount = controlsAmount;
        return slot;
    }

    public static FluidSlotWidget phantom(int capacity) {
        return phantom(new FluidTank(capacity > 0 ? capacity : 1), capacity > 1);
    }

    public FluidStack getContent() {
        return this.fluidTank.getFluid();
    }

    @Override
    public void onInit() {
        if (isClient()) {
            this.textRenderer.setShadow(true);
            this.textRenderer.setScale(0.5f);
        }
        if (getBackground() == null) {
            setBackground(ModularUITextures.FLUID_SLOT);
        }
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return SIZE;
    }

    public void setControlsAmount(boolean controlsAmount, boolean sync) {
        if (this.controlsAmount != controlsAmount) {
            this.controlsAmount = controlsAmount;
            if (sync) {
                if (isClient()) {
                    syncToServer(3, buffer -> buffer.writeBoolean(controlsAmount));
                } else {
                    syncToClient(3, buffer -> buffer.writeBoolean(controlsAmount));
                }
            }
        }
    }

    @Override
    public void buildTooltip(List<Text> tooltip) {
        super.buildTooltip(tooltip);
        FluidStack fluid = fluidTank.getFluid();
        if (phantom) {
            if (fluid != null) {
                addFluidNameInfo(tooltip, fluid);
                if (controlsAmount) {
                    tooltip.add(Text.localised("modularui.fluid.phantom.amount", fluid.amount));
                    addAdditionalFluidInfo(tooltip, fluid);
                    tooltip.add(Text.localised("modularui.fluid.phantom.control"));
                } else {
                    addAdditionalFluidInfo(tooltip, fluid);
                    tooltip.add(Text.localised("modularui.phantom.single.clear"));
                }
                if (!Interactable.hasShiftDown()) {
                    tooltip.add(Text.EMPTY); // Add an empty line to separate from the bottom material tooltips
                    tooltip.add(Text.localised("modularui.tooltip.shift"));
                }
            } else {
                tooltip.add(Text.localised("modularui.fluid.empty").format(EnumChatFormatting.WHITE));
            }
        } else {
            if (fluid != null) {
                addFluidNameInfo(tooltip, fluid);
                tooltip.add(Text.localised("modularui.fluid.amount", fluid.amount, fluidTank.getCapacity()));
                addAdditionalFluidInfo(tooltip, fluid);
            } else {
                tooltip.add(Text.localised("modularui.fluid.empty").format(EnumChatFormatting.WHITE));
                tooltip.add(Text.localised("modularui.fluid.capacity", fluidTank.getCapacity()));
            }
            if (canFillSlot || canDrainSlot) {
                tooltip.add(Text.EMPTY); // Add an empty line to separate from the bottom material tooltips
                if (Interactable.hasShiftDown()) {
                    if (canFillSlot && canDrainSlot) {
                        tooltip.add(Text.localised("modularui.fluid.click_combined"));
                    } else if (canDrainSlot) {
                        tooltip.add(Text.localised("modularui.fluid.click_to_fill"));
                    } else if (canFillSlot) {
                        tooltip.add(Text.localised("modularui.fluid.click_to_empty"));
                    }
                } else {
                    tooltip.add(Text.localised("modularui.tooltip.shift"));
                }
            }
        }
    }

    protected void addFluidNameInfo(List<Text> tooltip, @NotNull FluidStack fluid) {
        tooltip.add(new Text(fluid.getLocalizedName()).format(EnumChatFormatting.WHITE));
        if (isGT5ULoaded) {
            String formula = GT_FluidDisplayItem.getChemicalFormula(fluid);
            if (!formula.isEmpty()) {
                tooltip.add(new Text(formula).format(EnumChatFormatting.YELLOW));
            }
        }
        if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
            tooltip.add(
                    Text.localised("modularui.fluid.registry", fluid.getFluid().getName()));
        }
    }

    /**
     * Mods can override this to add custom tooltips for the fluid
     *
     * @param tooltipContainer add lines here
     * @param fluid            the nonnull fluid
     */
    public void addAdditionalFluidInfo(List<Text> tooltipContainer, @NotNull FluidStack fluid) {
        if (Interactable.hasShiftDown()) {
            tooltipContainer.add(Text.localised(
                    "modularui.fluid.temperature", fluid.getFluid().getTemperature(fluid)));
            tooltipContainer.add(Text.localised(
                    "modularui.fluid.state",
                    fluid.getFluid().isGaseous(fluid)
                            ? StatCollector.translateToLocal("modularui.fluid.gas")
                            : StatCollector.translateToLocal("modularui.fluid.liquid")));
        }
    }

    @Override
    public @Nullable String getBackgroundColorKey() {
        return Theme.KEY_FLUID_SLOT;
    }

    @Override
    public void draw(float partialTicks) {
        FluidStack content = fluidTank.getFluid();
        if (content != null) {
            float y = contentOffset.y;
            float height = size.height - contentOffset.y * 2;
            if (!alwaysShowFull) {
                float newHeight = height * content.amount * 1f / fluidTank.getCapacity();
                y += height - newHeight;
                height = newHeight;
            }
            GuiHelper.drawFluidTexture(content, contentOffset.x, y, size.width - contentOffset.x * 2, height, 0);
        }
        if (overlayTexture != null) {
            overlayTexture.draw(Pos2d.ZERO, size, partialTicks);
        }
        if (content != null && (!phantom || controlsAmount)) {
            String s = NumberFormat.format(content.amount) + "L";
            textRenderer.setAlignment(Alignment.CenterLeft, size.width - contentOffset.x - 1f);
            textRenderer.setPos((int) (contentOffset.x + 0.5f), (int) (size.height - 4.5f));
            textRenderer.draw(s);
        }
        if (isHovering() && !getContext().getCursor().hasDraggable()) {
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GlStateManager.colorMask(true, true, true, false);
            ModularGui.drawSolidRect(1, 1, 16, 16, Theme.INSTANCE.getSlotHighlight());
            GlStateManager.colorMask(true, true, true, true);
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    @Override
    public void onScreenUpdate() {
        if (lastShift != Interactable.hasShiftDown()) {
            lastShift = Interactable.hasShiftDown();
            notifyTooltipChange();
        }
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (!this.canFillSlot && !this.canDrainSlot) {
            return ClickResult.ACKNOWLEDGED;
        }
        ItemStack cursorStack = getContext().getCursor().getItemStack();
        if (phantom || cursorStack != null) {
            ClickData clickData = ClickData.create(buttonId, doubleClick);
            ItemStack verifyToken = tryClickContainer(clickData);
            syncToServer(1, buffer -> {
                clickData.writeToPacket(buffer);
                try {
                    buffer.writeItemStackToBuffer(verifyToken);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            if (playClickSound) {
                Interactable.playButtonClickSound();
            }
            return ClickResult.ACCEPT;
        }
        return ClickResult.ACKNOWLEDGED;
    }

    @Override
    public boolean onMouseScroll(int direction) {
        if (this.phantom) {
            if ((direction > 0 && !this.canFillSlot) || (direction < 0 && !this.canDrainSlot)) {
                return false;
            }
            if (Interactable.hasShiftDown()) {
                direction *= 10;
            }
            if (Interactable.hasControlDown()) {
                direction *= 100;
            }
            final int finalDirection = direction;
            syncToServer(2, buffer -> buffer.writeVarIntToBuffer(finalDirection));
            return true;
        }
        return false;
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        FluidStack currentFluid = this.fluidTank.getFluid();
        if (init || fluidChanged(currentFluid, this.lastStoredFluid)) {
            this.lastStoredFluid = currentFluid == null ? null : currentFluid.copy();
            syncToClient(1, buffer -> NetworkUtils.writeFluidStack(buffer, currentFluid));
            markForUpdate();
        }
    }

    public static boolean fluidChanged(@Nullable FluidStack current, @Nullable FluidStack cached) {
        return current == null ^ cached == null
                || (current != null && (current.amount != cached.amount || !current.isFluidEqual(cached)));
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == 1) {
            FluidStack fluidStack = NetworkUtils.readFluidStack(buf);
            fluidTank.drain(Integer.MAX_VALUE, true);
            fluidTank.fill(fluidStack, true);
            notifyTooltipChange();
        } else if (id == 3) {
            this.controlsAmount = buf.readBoolean();
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == 1) {
            onClickServer(ClickData.readPacket(buf), buf.readItemStackFromBuffer());
        } else if (id == 2) {
            if (this.phantom) {
                tryScrollPhantom(buf.readVarIntFromBuffer());
            }
        } else if (id == 3) {
            this.controlsAmount = buf.readBoolean();
        } else if (id == 4) {
            tryClickPhantom(ClickData.readPacket(buf), buf.readItemStackFromBuffer());
            if (onDragAndDropComplete != null) {
                onDragAndDropComplete.accept(this);
            }
        }
        markForUpdate();
    }

    protected void onClickServer(ClickData clickData, ItemStack clientVerifyToken) {
        ItemStack serverVerifyToken = tryClickContainer(clickData);
        // similar to what NetHandlerPlayServer#processClickWindow does
        if (!ItemStack.areItemStacksEqual(clientVerifyToken, serverVerifyToken)) {
            ((EntityPlayerMP) getContext().getPlayer())
                    .sendContainerToPlayer(getContext().getContainer());
        }
    }

    protected ItemStack tryClickContainer(ClickData clickData) {
        if (this.phantom) {
            tryClickPhantom(clickData);
            return null;
        } else {
            return transferFluid(clickData);
        }
    }

    protected ItemStack transferFluid(ClickData clickData) {
        EntityPlayer player = getContext().getPlayer();
        boolean processFullStack = clickData.mouseButton == 0;
        ItemStack heldItem = player.inventory.getItemStack();
        if (heldItem == null || heldItem.stackSize == 0) return null;

        ItemStack heldItemSizedOne = heldItem.copy();
        heldItemSizedOne.stackSize = 1;
        FluidStack currentFluid = fluidTank.getFluid();
        FluidStack heldFluid = getFluidForRealItem(heldItemSizedOne);
        if (heldFluid != null && heldFluid.amount <= 0) {
            heldFluid = null;
        }

        if (currentFluid == null) {
            // tank empty, consider fill only from now on
            if (!canFillSlot)
                // cannot fill and nothing to take, bail out
                return null;
            if (heldFluid == null)
                // no fluid to fill
                return null;
            return fillFluid(heldFluid, processFullStack);
        }

        // tank not empty, both action possible
        if (heldFluid != null && currentFluid.amount < fluidTank.getCapacity()) {
            // both nonnull and have space left for filling.
            if (canFillSlot)
                // actually both pickup and fill is reasonable, but I'll go with fill here
                return fillFluid(heldFluid, processFullStack);
            if (!canDrainSlot)
                // cannot take AND cannot fill, why make this call then?
                return null;
            // the slot does not allow filling, so try take some
            return drainFluid(processFullStack);
        } else {
            // cannot fill and there is something to take
            if (!canDrainSlot)
                // but the slot does not allow taking, so bail out
                return null;
            return drainFluid(processFullStack);
        }
    }

    protected ItemStack drainFluid(boolean processFullStack) {
        EntityPlayer player = getContext().getPlayer();
        ItemStack heldItem = player.inventory.getItemStack();
        if (heldItem == null || heldItem.stackSize == 0) return null;

        ItemStack heldItemSizedOne = heldItem.copy();
        heldItemSizedOne.stackSize = 1;
        FluidStack currentFluid = fluidTank.getFluid();
        if (currentFluid == null) return null;

        int originalFluidAmount = currentFluid.amount;
        ItemStack filledContainer = fillFluidContainer(currentFluid, heldItemSizedOne);
        if (filledContainer != null) {
            int filledAmount = originalFluidAmount - currentFluid.amount;
            fluidTank.drain(filledAmount, true);
            if (processFullStack) {
                /*
                 Work out how many more items we can fill.
                 One cell is already used, so account for that.
                 The round down behavior will leave over a fraction of a cell worth of fluid.
                 The user then get to decide what to do with it.
                 It will not be too fancy if it spills out partially filled cells.
                */
                int additionalParallel = Math.min(heldItem.stackSize - 1, currentFluid.amount / filledAmount);
                fluidTank.drain(filledAmount * additionalParallel, true);
                filledContainer.stackSize += additionalParallel;
            }
            replaceCursorItemStack(filledContainer);
            playSound(currentFluid, false);
        }
        return filledContainer;
    }

    protected ItemStack fillFluid(FluidStack heldFluid, boolean processFullStack) {
        EntityPlayer player = getContext().getPlayer();
        ItemStack heldItem = player.inventory.getItemStack();
        if (heldItem == null || heldItem.stackSize == 0) return null;

        ItemStack heldItemSizedOne = heldItem.copy();
        heldItemSizedOne.stackSize = 1;
        FluidStack currentFluid = fluidTank.getFluid();
        // we are not using aMachine.fill() here anymore, so we need to check for fluid type here ourselves
        if (currentFluid != null && !currentFluid.isFluidEqual(heldFluid)) return null;

        int freeSpace = fluidTank.getCapacity() - (currentFluid != null ? currentFluid.amount : 0);
        if (freeSpace <= 0)
            // no space left
            return null;

        // find out how much fluid can be taken
        // some cells cannot be partially filled
        ItemStack itemStackEmptied = null;
        int fluidAmountTaken = 0;
        if (freeSpace >= heldFluid.amount) {
            // fully accepted - try take it from item now
            // IFluidContainerItem is intentionally not checked here. it will be checked later
            itemStackEmptied = getContainerForFilledItemWithoutIFluidContainerItem(heldItemSizedOne);
            fluidAmountTaken = heldFluid.amount;
        }
        if (itemStackEmptied == null && heldItemSizedOne.getItem() instanceof IFluidContainerItem) {
            // either partially accepted, or is IFluidContainerItem
            IFluidContainerItem container = (IFluidContainerItem) heldItemSizedOne.getItem();
            FluidStack tDrained = container.drain(heldItemSizedOne, freeSpace, true);
            if (tDrained != null && tDrained.amount > 0) {
                // something is actually drained - change the cell and drop it to player
                itemStackEmptied = heldItemSizedOne;
                fluidAmountTaken = tDrained.amount;
            }
        }
        if (itemStackEmptied == null)
            // somehow the cell refuse to give out that amount of fluid, no op then
            return null;

        // find out how many fill can we do
        // same round down behavior as above
        // however here the fluid stack is not changed at all, so the exact code will slightly differ
        int parallel = processFullStack ? Math.min(freeSpace / fluidAmountTaken, heldItem.stackSize) : 1;
        FluidStack copiedFluidStack = heldFluid.copy();
        copiedFluidStack.amount = fluidAmountTaken * parallel;
        fluidTank.fill(copiedFluidStack, true);

        itemStackEmptied.stackSize = parallel;
        replaceCursorItemStack(itemStackEmptied);
        playSound(heldFluid, true);
        return itemStackEmptied;
    }

    protected void replaceCursorItemStack(ItemStack resultStack) {
        EntityPlayer player = getContext().getPlayer();
        int resultStackMaxStackSize = resultStack.getMaxStackSize();
        while (resultStack.stackSize > resultStackMaxStackSize) {
            player.inventory.getItemStack().stackSize -= resultStackMaxStackSize;
            addItemToPlayerInventory(player, resultStack.splitStack(resultStackMaxStackSize));
        }
        if (player.inventory.getItemStack().stackSize == resultStack.stackSize) {
            // every cell is mutated. it could just stay on the cursor.
            player.inventory.setItemStack(resultStack);
        } else {
            // some cells not mutated. The mutated cells must go into the inventory
            // or drop into the world if there isn't enough space.
            ItemStack tStackHeld = player.inventory.getItemStack();
            tStackHeld.stackSize -= resultStack.stackSize;
            addItemToPlayerInventory(player, resultStack);
        }
    }

    protected void tryClickPhantom(ClickData clickData) {
        tryClickPhantom(clickData, getContext().getCursor().getItemStack());
    }

    protected void tryClickPhantom(ClickData clickData, ItemStack cursorStack) {
        FluidStack currentFluid = fluidTank.getFluid();

        if (clickData.mouseButton == 0) {
            if (cursorStack == null) {
                if (canDrainSlot) {
                    fluidTank.drain(clickData.shift ? Integer.MAX_VALUE : 1000, true);
                }
            } else {
                ItemStack heldItemSizedOne = cursorStack.copy();
                heldItemSizedOne.stackSize = 1;
                FluidStack heldFluid = getFluidForPhantomItem(heldItemSizedOne);
                if ((controlsAmount || currentFluid == null) && heldFluid != null) {
                    if (canFillSlot) {
                        if (!controlsAmount) {
                            heldFluid.amount = 1;
                        }
                        if (fluidTank.fill(heldFluid, true) > 0) {
                            lastStoredPhantomFluid = heldFluid.copy();
                        }
                    }
                } else {
                    if (canDrainSlot) {
                        fluidTank.drain(clickData.shift ? Integer.MAX_VALUE : 1000, true);
                    }
                }
            }
        } else if (clickData.mouseButton == 1) {
            if (canFillSlot) {
                if (currentFluid != null) {
                    if (controlsAmount) {
                        FluidStack toFill = currentFluid.copy();
                        toFill.amount = 1000;
                        fluidTank.fill(toFill, true);
                    }
                } else if (lastStoredPhantomFluid != null) {
                    FluidStack toFill = lastStoredPhantomFluid.copy();
                    toFill.amount = controlsAmount ? 1000 : 1;
                    fluidTank.fill(toFill, true);
                }
            }
        } else if (clickData.mouseButton == 2 && currentFluid != null && canDrainSlot) {
            fluidTank.drain(clickData.shift ? Integer.MAX_VALUE : 1000, true);
        }
    }

    protected void tryScrollPhantom(int direction) {
        FluidStack currentFluid = this.fluidTank.getFluid();
        if (currentFluid == null) {
            if (direction > 0 && this.lastStoredPhantomFluid != null) {
                FluidStack toFill = this.lastStoredPhantomFluid.copy();
                toFill.amount = this.controlsAmount ? direction : 1;
                this.fluidTank.fill(toFill, true);
            }
            return;
        }
        if (direction > 0 && this.controlsAmount) {
            FluidStack toFill = currentFluid.copy();
            toFill.amount = direction;
            this.fluidTank.fill(toFill, true);
        } else if (direction < 0) {
            this.fluidTank.drain(-direction, true);
        }
    }

    /**
     * In 1.7.10 placing water or lava does not play sound, so we do nothing here.
     * Override if you want to play something.
     */
    protected void playSound(FluidStack fluid, boolean fill) {}

    @Override
    public boolean handleDragAndDrop(ItemStack draggedStack, int button) {
        if (!isPhantom()) return false;
        ClickData clickData = ClickData.create(button, false);
        tryClickPhantom(clickData, draggedStack);
        syncToServer(4, buffer -> {
            try {
                clickData.writeToPacket(buffer);
                buffer.writeItemStackToBuffer(draggedStack);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        draggedStack.stackSize = 0;
        return true;
    }

    /**
     * Gets fluid actually stored in item. Used for transferring fluid.
     */
    protected FluidStack getFluidForRealItem(ItemStack itemStack) {
        if (isGT5ULoaded) {
            return GT_Utility.getFluidForFilledItem(itemStack, true);
        } else {
            FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(itemStack);
            if (fluidStack == null && itemStack.getItem() instanceof IFluidContainerItem) {
                fluidStack = ((IFluidContainerItem) itemStack.getItem()).getFluid(itemStack);
            }
            return fluidStack;
        }
    }

    /**
     * Gets fluid for use in phantom slot.
     */
    protected FluidStack getFluidForPhantomItem(ItemStack itemStack) {
        if (isGT5ULoaded) {
            return GT_Utility.getFluidFromContainerOrFluidDisplay(itemStack);
        } else if (isNEILoaded) {
            return StackInfo.getFluid(itemStack);
        } else {
            return getFluidForRealItem(itemStack);
        }
    }

    protected ItemStack fillFluidContainer(FluidStack fluidStack, ItemStack itemStack) {
        ItemStack filledContainer = fillFluidContainerWithoutIFluidContainerItem(fluidStack, itemStack);
        if (filledContainer == null) {
            filledContainer = fillFluidContainerWithIFluidContainerItem(fluidStack, itemStack);
        }
        return filledContainer;
    }

    protected ItemStack fillFluidContainerWithoutIFluidContainerItem(FluidStack fluidStack, ItemStack itemStack) {
        if (isGT5ULoaded) {
            return GT_Utility.fillFluidContainer(fluidStack, itemStack, true, false);
        }
        return null;
    }

    protected ItemStack fillFluidContainerWithIFluidContainerItem(FluidStack fluidStack, ItemStack itemStack) {
        if (itemStack.getItem() instanceof IFluidContainerItem) {
            IFluidContainerItem tContainerItem = (IFluidContainerItem) itemStack.getItem();
            int tFilledAmount = tContainerItem.fill(itemStack, fluidStack, true);
            if (tFilledAmount > 0) {
                fluidStack.amount -= tFilledAmount;
                return itemStack;
            }
        }
        return null;
    }

    protected ItemStack getContainerForFilledItemWithoutIFluidContainerItem(ItemStack itemStack) {
        if (isGT5ULoaded) {
            return GT_Utility.getContainerForFilledItem(itemStack, false);
        }
        return null;
    }

    protected static void addItemToPlayerInventory(EntityPlayer player, ItemStack stack) {
        if (stack == null) return;
        if (!player.inventory.addItemStackToInventory(stack) && !player.worldObj.isRemote) {
            EntityItem dropItem = player.entityDropItem(stack, 0);
            dropItem.delayBeforeCanPickup = 0;
        }
    }

    public boolean canFillSlot() {
        return canFillSlot;
    }

    public boolean canDrainSlot() {
        return canDrainSlot;
    }

    public boolean alwaysShowFull() {
        return alwaysShowFull;
    }

    public Pos2d getContentOffset() {
        return contentOffset;
    }

    public boolean controlsAmount() {
        return controlsAmount;
    }

    public boolean isPhantom() {
        return phantom;
    }

    @Nullable
    public IDrawable getOverlayTexture() {
        return overlayTexture;
    }

    @Override
    public ItemStack getStackUnderMouse() {
        if (isGT5ULoaded) {
            return GT_Utility.getFluidDisplayStack(fluidTank.getFluid(), false);
        }
        return null;
    }

    public FluidSlotWidget setInteraction(boolean canDrainSlot, boolean canFillSlot) {
        this.canDrainSlot = canDrainSlot;
        this.canFillSlot = canFillSlot;
        return this;
    }

    public FluidSlotWidget setAlwaysShowFull(boolean alwaysShowFull) {
        this.alwaysShowFull = alwaysShowFull;
        return this;
    }

    public FluidSlotWidget setContentOffset(Pos2d contentOffset) {
        this.contentOffset = contentOffset;
        return this;
    }

    public FluidSlotWidget setOverlayTexture(@Nullable IDrawable overlayTexture) {
        this.overlayTexture = overlayTexture;
        return this;
    }

    public FluidSlotWidget setPlayClickSound(boolean playClickSound) {
        this.playClickSound = playClickSound;
        return this;
    }

    public FluidSlotWidget setOnDragAndDropComplete(Consumer<Widget> onDragAndDropComplete) {
        this.onDragAndDropComplete = onDragAndDropComplete;
        return this;
    }
}
