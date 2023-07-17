package com.gtnewhorizons.modularui.common.widget;

import static com.google.common.primitives.Ints.saturatedCast;
import static com.gtnewhorizons.modularui.ModularUI.isGT5ULoaded;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidTank;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.NumberFormat;
import com.gtnewhorizons.modularui.api.drawable.GuiHelper;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.drawable.TextRenderer;
import com.gtnewhorizons.modularui.api.fluids.FluidTankLong;
import com.gtnewhorizons.modularui.api.fluids.FluidTanksHandler;
import com.gtnewhorizons.modularui.api.fluids.IFluidTankLong;
import com.gtnewhorizons.modularui.api.fluids.IFluidTanksHandler;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.FluidInteractionUtil;
import com.gtnewhorizons.modularui.api.widget.IDragAndDropHandler;
import com.gtnewhorizons.modularui.api.widget.IHasStackUnderMouse;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.Theme;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;

import gregtech.api.util.GT_Utility;

@SuppressWarnings("unused")
public class FluidSlotWidget extends SyncedWidget
        implements Interactable, IDragAndDropHandler, IHasStackUnderMouse, FluidInteractionUtil {

    public static final Size SIZE = new Size(18, 18);
    private static final int PACKET_REAL_CLICK = 1, PACKET_SCROLL = 2, PACKET_CONTROLS_AMOUNT = 3,
            PACKET_DRAG_AND_DROP = 4, PACKET_SYNC_FLUID = 5;

    @Nullable
    private IDrawable overlayTexture;

    private final TextRenderer textRenderer = new TextRenderer();
    private final IFluidTanksHandler handler;
    private final int tank;

    @Nullable
    private IFluidTankLong lastStoredFluid;

    private IFluidTankLong lastStoredPhantomFluid;
    private Pos2d contentOffset = new Pos2d(1, 1);
    private boolean alwaysShowFull = true;
    private boolean canDrainSlot = true;
    private boolean canFillSlot = true;
    private boolean phantom = false;
    private boolean controlsAmount = false;
    private boolean lastShift = false;
    private boolean playClickSound = false;
    private Function<Fluid, Boolean> filter = fluid -> true;

    private Consumer<FluidSlotWidget> onClickContainer;
    private Consumer<Widget> onDragAndDropComplete;

    public FluidSlotWidget(IFluidTanksHandler handler, int tank) {
        this.handler = handler;
        this.tank = tank;
        this.textRenderer.setColor(Color.WHITE.normal);
        this.textRenderer.setShadow(true);
    }

    public FluidSlotWidget(IFluidTank fluidTank) {
        this(new FluidTanksHandler(new FluidTankLong(fluidTank)), 0);
    }

    public static FluidSlotWidget phantom(IFluidTanksHandler handler, int tank, boolean controlsAmount) {
        FluidSlotWidget slot = new FluidSlotWidget(handler, tank);
        slot.phantom = true;
        slot.controlsAmount = controlsAmount;
        return slot;
    }

    public static FluidSlotWidget phantom(IFluidTank fluidTank, boolean controlsAmount) {
        return phantom(new FluidTanksHandler(new FluidTankLong(fluidTank)), 0, controlsAmount);
    }

    public static FluidSlotWidget phantom(int capacity) {
        return phantom(new FluidTank(capacity > 0 ? capacity : 1), capacity > 1);
    }

    public FluidStack getContent() {
        return handler.getFluidStackInTank(tank);
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
                    syncToServer(PACKET_CONTROLS_AMOUNT, buffer -> buffer.writeBoolean(controlsAmount));
                } else {
                    syncToClient(PACKET_CONTROLS_AMOUNT, buffer -> buffer.writeBoolean(controlsAmount));
                }
            }
        }
    }

    @Override
    public void buildTooltip(List<Text> tooltip) {
        super.buildTooltip(tooltip);
        FluidStack fluid = handler.getFluidStackInTank(tank);
        if (phantom) {
            if (fluid != null) {
                addFluidNameInfo(tooltip, fluid);
                if (controlsAmount) {
                    tooltip.add(Text.localised("modularui.fluid.phantom.amount", handler.getTankStoredAmount(tank)));
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
                tooltip.add(
                        Text.localised(
                                "modularui.fluid.amount",
                                handler.getTankStoredAmount(tank),
                                handler.getTankCapacity(tank)));
                addAdditionalFluidInfo(tooltip, fluid);
            } else {
                tooltip.add(Text.localised("modularui.fluid.empty").format(EnumChatFormatting.WHITE));
                tooltip.add(Text.localised("modularui.fluid.capacity", handler.getTankCapacity(tank)));
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

    @Override
    public @Nullable String getBackgroundColorKey() {
        return Theme.KEY_FLUID_SLOT;
    }

    @Override
    public void draw(float partialTicks) {
        FluidStack content = handler.getFluidStackInTank(tank);
        if (content != null) {
            float y = contentOffset.y;
            float height = size.height - contentOffset.y * 2;
            if (!alwaysShowFull) {
                float newHeight = height * handler.getTankStoredAmount(tank) * 1f / handler.getTankCapacity(tank);
                y += height - newHeight;
                height = newHeight;
            }
            GuiHelper.drawFluidTexture(content, contentOffset.x, y, size.width - contentOffset.x * 2, height, 0);
        }
        if (overlayTexture != null) {
            overlayTexture.draw(Pos2d.ZERO, size, partialTicks);
        }
        if (content != null && (!phantom || controlsAmount)) {
            String s = NumberFormat.formatLong(handler.getTankStoredAmount(tank)) + "L";
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
            if (onClickContainer != null) {
                onClickContainer.accept(this);
            }
            syncToServer(PACKET_REAL_CLICK, buffer -> {
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
            syncToServer(PACKET_SCROLL, buffer -> buffer.writeVarIntToBuffer(finalDirection));
            return true;
        }
        return false;
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        IFluidTankLong currentFluid = handler.getFluidTank(tank);
        if (init || fluidChanged(currentFluid, this.lastStoredFluid)) {
            this.lastStoredFluid = currentFluid == null ? null : currentFluid.copy();
            syncToClient(PACKET_SYNC_FLUID, buffer -> FluidTankLong.writeToBuffer(buffer, currentFluid));
            markForUpdate();
        }
    }

    public static boolean fluidChanged(@Nullable IFluidTankLong currentFluid, @Nullable IFluidTankLong cached) {
        return currentFluid == null ^ cached == null || (currentFluid != null
                && (currentFluid.getFluidAmountLong() != cached.getFluidAmountLong() || !currentFluid.isFluidEqual(cached)));
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == PACKET_SYNC_FLUID) {
            FluidTankLong.readFromBuffer(buf, handler.getFluidTank(id));
            notifyTooltipChange();
        } else if (id == PACKET_CONTROLS_AMOUNT) {
            this.controlsAmount = buf.readBoolean();
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == PACKET_REAL_CLICK) {
            onClickServer(ClickData.readPacket(buf), buf.readItemStackFromBuffer());
        } else if (id == PACKET_SCROLL) {
            if (this.phantom) {
                tryScrollPhantom(buf.readVarIntFromBuffer());
            }
        } else if (id == PACKET_CONTROLS_AMOUNT) {
            this.controlsAmount = buf.readBoolean();
        } else if (id == PACKET_DRAG_AND_DROP) {
            tryClickPhantom(ClickData.readPacket(buf), buf.readItemStackFromBuffer());
            if (onDragAndDropComplete != null) {
                onDragAndDropComplete.accept(this);
            }
        }
        markForUpdate();
    }

    protected void onClickServer(ClickData clickData, ItemStack clientVerifyToken) {
        ItemStack serverVerifyToken = tryClickContainer(clickData);
        if (onClickContainer != null) {
            onClickContainer.accept(this);
        }
        // similar to what NetHandlerPlayServer#processClickWindow does
        if (!ItemStack.areItemStacksEqual(clientVerifyToken, serverVerifyToken)) {
            ((EntityPlayerMP) getContext().getPlayer()).sendContainerToPlayer(getContext().getContainer());
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
        FluidStack currentFluid = handler.getFluidStackInTank(tank);
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
        if (heldFluid != null && handler.getTankStoredAmount(tank) < handler.getTankCapacity(tank)) {
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
        FluidStack currentFluid = handler.getFluidStackInTank(tank);
        if (currentFluid == null) return null;
        // We want to see how much fluid is drained without modifying original fluidstack.
        currentFluid = currentFluid.copy();

        long originalFluidAmount = handler.getTankStoredAmount(tank);
        ItemStack filledContainer = fillFluidContainer(currentFluid, heldItemSizedOne);
        if (filledContainer != null) {
            long filledAmount = originalFluidAmount - currentFluid.amount;
            if (filledAmount < 1) {
                ModularUI.logger.warn(
                        "Item {} returned filled item {}, but no fluid was actually drained.",
                        heldItemSizedOne.getDisplayName(),
                        filledContainer.getDisplayName());
                return null;
            }
            handler.drain(tank, filledAmount, false);
            if (processFullStack) {
                /*
                 * Work out how many more items we can fill. One cell is already used, so account for that. The round
                 * down behavior will leave over a fraction of a cell worth of fluid. The user then get to decide what
                 * to do with it. It will not be too fancy if it spills out partially filled cells.
                 */
                long additionalParallel = Math.min(heldItem.stackSize - 1, currentFluid.amount / filledAmount);
                handler.drain(tank, filledAmount * additionalParallel, false);
                filledContainer.stackSize += additionalParallel;
            }
            replaceCursorItemStack(filledContainer);
            playSound(currentFluid, false);
        }
        return filledContainer;
    }

    protected ItemStack fillFluid(@NotNull FluidStack heldFluid, boolean processFullStack) {
        EntityPlayer player = getContext().getPlayer();
        ItemStack heldItem = player.inventory.getItemStack();
        if (heldItem == null || heldItem.stackSize == 0) return null;

        ItemStack heldItemSizedOne = heldItem.copy();
        heldItemSizedOne.stackSize = 1;
        FluidStack currentFluid = handler.getFluidStackInTank(tank);
        // we are not using aMachine.fill() here anymore, so we need to check for fluid type here ourselves
        if (currentFluid != null && !currentFluid.isFluidEqual(heldFluid)) return null;
        if (!filter.apply(heldFluid.getFluid())) return null;

        long freeSpace = handler.getTankCapacity(tank) - handler.getTankStoredAmount(tank);
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
            FluidStack tDrained = container.drain(heldItemSizedOne, saturatedCast(freeSpace), true);
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
        long parallel = processFullStack ? Math.min(freeSpace / fluidAmountTaken, heldItem.stackSize) : 1;
        FluidStack copiedFluidStack = heldFluid.copy();
        handler.fill(tank, copiedFluidStack.getFluid(), fluidAmountTaken * parallel, false);

        itemStackEmptied.stackSize = saturatedCast(parallel);
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
        FluidStack currentFluid = handler.getFluidStackInTank(tank);

        if (clickData.mouseButton == 0) {
            if (cursorStack == null) {
                if (canDrainSlot) {
                    handler.drain(tank, clickData.shift ? Integer.MAX_VALUE : 1000, false);
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
                        if (handler.fill(tank, heldFluid.getFluid(), heldFluid.amount, true).amount > 0) {
                            lastStoredPhantomFluid = new FluidTankLong(
                                    heldFluid,
                                    handler.getTankCapacity(tank),
                                    handler.getTankStoredAmount(tank));
                        }
                    }
                } else {
                    if (canDrainSlot) {
                        handler.drain(tank, clickData.shift ? Integer.MAX_VALUE : 1000, false);
                    }
                }
            }
        } else if (clickData.mouseButton == 1) {
            if (canFillSlot) {
                if (currentFluid != null) {
                    if (controlsAmount) {
                        FluidStack toFill = currentFluid.copy();
                        toFill.amount = 1000;
                        handler.fill(tank, toFill.getFluid(), toFill.amount, false);
                    }
                } else if (lastStoredPhantomFluid != null) {
                    handler.fill(tank, lastStoredPhantomFluid.getStoredFluid(), controlsAmount ? 1000 : 1, false);
                }
            }
        } else if (clickData.mouseButton == 2 && currentFluid != null && canDrainSlot) {
            handler.drain(tank, clickData.shift ? Integer.MAX_VALUE : 1000, false);
        }
    }

    protected void tryScrollPhantom(int direction) {
        FluidStack currentFluid = handler.getFluidStackInTank(tank);
        if (currentFluid == null) {
            if (direction > 0 && this.lastStoredPhantomFluid != null) {
                handler.fill(tank, lastStoredPhantomFluid.getStoredFluid(), this.controlsAmount ? direction : 1, false);
            }
            return;
        }
        if (direction > 0 && this.controlsAmount) {
            FluidStack toFill = currentFluid.copy();
            toFill.amount = direction;
            handler.fill(tank, toFill.getFluid(), toFill.amount, false);
        } else if (direction < 0) {
            handler.drain(tank, direction, false);
        }
    }

    /**
     * In 1.7.10 placing water or lava does not play sound, so we do nothing here. Override if you want to play
     * something.
     */
    protected void playSound(FluidStack fluid, boolean fill) {}

    @Override
    public boolean handleDragAndDrop(ItemStack draggedStack, int button) {
        if (!isPhantom()) return false;
        ClickData clickData = ClickData.create(button, false);
        tryClickPhantom(clickData, draggedStack);
        syncToServer(PACKET_DRAG_AND_DROP, buffer -> {
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
            return GT_Utility.getFluidDisplayStack(handler.getFluidStackInTank(tank), false);
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

    public FluidSlotWidget setFilter(Function<Fluid, Boolean> filter) {
        this.filter = filter;
        return this;
    }

    public FluidSlotWidget setOnClickContainer(Consumer<FluidSlotWidget> onClickContainer) {
        this.onClickContainer = onClickContainer;
        return this;
    }

    public FluidSlotWidget setOnDragAndDropComplete(Consumer<Widget> onDragAndDropComplete) {
        this.onDragAndDropComplete = onDragAndDropComplete;
        return this;
    }
}
