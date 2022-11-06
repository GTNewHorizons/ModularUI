package com.gtnewhorizons.modularui.common.widget;

import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.NumberFormat;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.drawable.TextRenderer;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.IDragAndDropHandler;
import com.gtnewhorizons.modularui.api.widget.ISyncedWidget;
import com.gtnewhorizons.modularui.api.widget.IVanillaSlot;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.Theme;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.internal.wrapper.GuiContainerAccessor;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

public class SlotWidget extends Widget implements IVanillaSlot, Interactable, ISyncedWidget, IDragAndDropHandler {

    private boolean needsUpdate;

    public static final Size SIZE = new Size(18, 18);

    private final TextRenderer textRenderer = new TextRenderer();
    private final BaseSlot slot;
    private ItemStack lastStoredPhantomItem = null;

    protected boolean interactionDisabled = false;
    protected boolean handlePhantomActionClient = false;

    protected boolean controlsAmount = false;
    private Function<List<String>, List<String>> overwriteItemStackTooltip;

    protected Consumer<Widget> onDragAndDropComplete;

    @Nullable
    private String sortAreaName = null;

    public SlotWidget(BaseSlot slot) {
        this.slot = slot;
        slot.setParentWidget(this);
    }

    public SlotWidget(IItemHandlerModifiable handler, int index) {
        this(new BaseSlot(handler, index, false));
    }

    public static SlotWidget phantom(IItemHandlerModifiable handler, int index) {
        return new SlotWidget(BaseSlot.phantom(handler, index));
    }

    public static SlotWidget empty() {
        return new SlotWidget(BaseSlot.empty());
    }

    @Override
    public void onInit() {
        getContext().getContainer().addSlotToContainer(this.slot);
        if (getBackground() == null) {
            setBackground(ModularUITextures.ITEM_SLOT);
        }
        if (!isClient() && this.slot.getStack() != null) {
            this.lastStoredPhantomItem = this.slot.getStack().copy();
        }
    }

    @Override
    public void onDestroy() {
        getContext().getContainer().removeSlot(this.slot);
    }

    @Override
    public BaseSlot getMcSlot() {
        return this.slot;
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return SIZE;
    }

    @Override
    public @Nullable String getBackgroundColorKey() {
        return Theme.KEY_ITEM_SLOT;
    }

    @Override
    public Function<List<String>, List<String>> getOverwriteItemStackTooltip() {
        return overwriteItemStackTooltip;
    }

    @Override
    public void draw(float partialTicks) {
        RenderHelper.enableGUIStandardItemLighting();
        drawSlot(this.slot);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableLighting();
        if (isHovering()) {
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GlStateManager.colorMask(true, true, true, false);
            ModularGui.drawSolidRect(1, 1, 16, 16, Theme.INSTANCE.getSlotHighlight());
            GlStateManager.colorMask(true, true, true, true);
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    @Override
    public void onRebuild() {
        Pos2d pos =
                getAbsolutePos().subtract(getContext().getMainWindow().getPos()).add(1, 1);
        if (this.slot.xDisplayPosition != pos.x || this.slot.yDisplayPosition != pos.y) {
            this.slot.xDisplayPosition = pos.x;
            this.slot.yDisplayPosition = pos.y;
        }
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (init || this.slot.isNeedsSyncing()) {
            getContext().syncSlotContent(this.slot);
            if (this.slot.isNeedsSyncing()) {
                markForUpdate();
            }
            this.slot.resetNeedsSyncing();
        }
    }

    @Override
    public void buildTooltip(List<Text> tooltip) {
        super.buildTooltip(tooltip);
        if (isPhantom()) {
            if (canControlAmount()) {
                tooltip.add(Text.localised("modularui.item.phantom.control"));
            }
        }
    }

    @Override
    public List<String> getExtraTooltip() {
        List<String> extraLines = new ArrayList<>();
        if (slot.getStack().stackSize >= 1000) {
            extraLines.add(I18n.format("modularui.amount", slot.getStack().stackSize));
        }
        if (isPhantom()) {
            if (canControlAmount()) {
                String[] lines = I18n.format("modularui.item.phantom.control").split("\\\\n");
                extraLines.addAll(Arrays.asList(lines));
            } else if (!interactionDisabled) {
                extraLines.add(I18n.format("modularui.phantom.single.clear"));
            }
        }
        return extraLines.isEmpty() ? Collections.emptyList() : extraLines;
    }

    public boolean isPhantom() {
        return this.slot.isPhantom();
    }

    @Override
    public SlotWidget setPos(Pos2d relativePos) {
        return (SlotWidget) super.setPos(relativePos);
    }

    @Override
    public SlotWidget setSize(Size size) {
        return (SlotWidget) super.setSize(size);
    }

    /**
     * Apply arbitrary lambda for slot.
     */
    public SlotWidget applyForSlot(Consumer<BaseSlot> consumer) {
        consumer.accept(this.slot);
        return this;
    }

    public SlotWidget setShiftClickPriority(int priority) {
        this.slot.setShiftClickPriority(priority);
        return this;
    }

    public SlotWidget disableShiftInsert() {
        this.slot.disableShiftInsert();
        return this;
    }

    /**
     * Disables slot click, shift insert, and mouse scroll.
     * Does not prevent NEI recipe view or bookmark, but does block drag-and-drop.
     */
    public SlotWidget disableInteraction() {
        this.interactionDisabled = true;
        disableShiftInsert();
        return this;
    }

    /**
     * @param handlePhantomActionClient If phantom click and scroll are handled on client too
     */
    public SlotWidget setHandlePhantomActionClient(boolean handlePhantomActionClient) {
        this.handlePhantomActionClient = handlePhantomActionClient;
        return this;
    }

    public SlotWidget setChangeListener(Runnable runnable) {
        this.slot.setChangeListener(runnable);
        return this;
    }

    public SlotWidget setChangeListener(Consumer<SlotWidget> changeListener) {
        return setChangeListener(() -> changeListener.accept(this));
    }

    public SlotWidget setFilter(Predicate<ItemStack> filter) {
        this.slot.setFilter(filter);
        return this;
    }

    public SlotWidget setAccess(boolean canTake, boolean canInsert) {
        this.slot.setAccess(canTake, canInsert);
        return this;
    }

    public SlotWidget setIgnoreStackSizeLimit(boolean ignoreStackSizeLimit) {
        this.slot.setIgnoreStackSizeLimit(ignoreStackSizeLimit);
        return this;
    }

    public SlotWidget setSortable(String areaName) {
        if (this.sortAreaName == null ^ areaName == null) {
            this.sortAreaName = areaName;
        }
        return this;
    }

    @Override
    public SlotWidget setEnabled(boolean enabled) {
        if (enabled != isEnabled()) {
            super.setEnabled(enabled);
            slot.setEnabled(enabled);
            if (isClient()) {
                syncToServer(4, buffer -> buffer.writeBoolean(enabled));
            }
        }
        return this;
    }

    public SlotWidget setControlsAmount(boolean controlsAmount) {
        this.controlsAmount = controlsAmount;
        return this;
    }

    public SlotWidget setOverwriteItemStackTooltip(Function<List<String>, List<String>> overwriteItemStackTooltip) {
        this.overwriteItemStackTooltip = overwriteItemStackTooltip;
        return this;
    }

    public SlotWidget setOnDragAndDropComplete(Consumer<Widget> onDragAndDropComplete) {
        this.onDragAndDropComplete = onDragAndDropComplete;
        return this;
    }

    public boolean canControlAmount() {
        return controlsAmount && slot.getSlotStackLimit() > 1;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {}

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == 1) {
            this.slot.xDisplayPosition = buf.readVarIntFromBuffer();
            this.slot.yDisplayPosition = buf.readVarIntFromBuffer();
        } else if (id == 2) {
            phantomClick(ClickData.readPacket(buf));
        } else if (id == 3) {
            phantomScroll(buf.readVarIntFromBuffer());
        } else if (id == 4) {
            setEnabled(buf.readBoolean());
        } else if (id == 5) {
            handleDragAndDropServer(ClickData.readPacket(buf), buf.readItemStackFromBuffer());
            if (onDragAndDropComplete != null) {
                onDragAndDropComplete.accept(this);
            }
        }
        markForUpdate();
    }

    @Override
    public void markForUpdate() {
        needsUpdate = true;
    }

    @Override
    public void unMarkForUpdate() {
        needsUpdate = false;
    }

    @Override
    public boolean isMarkedForUpdate() {
        return needsUpdate;
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (interactionDisabled) return ClickResult.REJECT;
        if (isPhantom()) {
            ClickData clickData = ClickData.create(buttonId, doubleClick);
            syncToServer(2, clickData::writeToPacket);
            if (handlePhantomActionClient) {
                phantomClick(clickData);
            }
            return ClickResult.ACCEPT;
        }
        return ClickResult.REJECT;
    }

    @Override
    public boolean onMouseScroll(int direction) {
        if (interactionDisabled) return false;
        if (isPhantom()) {
            if (Interactable.hasShiftDown()) {
                direction *= 8;
            }
            final int finalDirection = direction;
            syncToServer(3, buffer -> buffer.writeVarIntToBuffer(finalDirection));
            if (handlePhantomActionClient) {
                phantomScroll(finalDirection);
            }
            return true;
        }
        return false;
    }

    protected void phantomClick(ClickData clickData) {
        phantomClick(clickData, getContext().getCursor().getItemStack());
    }

    protected void phantomClick(ClickData clickData, ItemStack cursorStack) {
        ItemStack slotStack = getMcSlot().getStack();
        ItemStack stackToPut;
        if (slotStack == null) {
            if (cursorStack == null) {
                if (clickData.mouseButton == 1 && this.lastStoredPhantomItem != null) {
                    stackToPut = this.lastStoredPhantomItem.copy();
                } else {
                    return;
                }
            } else {
                stackToPut = cursorStack.copy();
            }
            putClickedStack(stackToPut, clickData.mouseButton);
        } else {
            if (cursorStack == null) {
                if (clickData.mouseButton == 0) {
                    if (clickData.shift) {
                        this.slot.putStack(null);
                    } else {
                        this.slot.incrementStackCount(-1);
                    }
                } else if (clickData.mouseButton == 1) {
                    this.slot.incrementStackCount(1);
                }
            } else {
                putClickedStack(cursorStack.copy(), clickData.mouseButton);
            }
        }
    }

    protected void putClickedStack(ItemStack stack, int mouseButton) {
        if (!slot.isItemValidPhantom(stack)) return;
        if (mouseButton == 1 || !canControlAmount()) {
            stack.stackSize = 1;
        }
        stack.stackSize = Math.min(stack.stackSize, slot.getItemStackLimit(stack));
        slot.putStack(stack);
        this.lastStoredPhantomItem = stack.copy();
    }

    protected void phantomScroll(int direction) {
        ItemStack currentItem = this.slot.getStack();
        if (direction > 0 && currentItem == null && lastStoredPhantomItem != null) {
            ItemStack stackToPut = this.lastStoredPhantomItem.copy();
            stackToPut.stackSize = direction;
            this.slot.putStack(stackToPut);
        } else {
            this.slot.incrementStackCount(direction);
        }
    }

    @Override
    public boolean handleDragAndDrop(ItemStack draggedStack, int button) {
        if (interactionDisabled) return false;
        if (!isPhantom()) return false;
        ClickData clickData = ClickData.create(button, false);
        syncToServer(5, buffer -> {
            try {
                clickData.writeToPacket(buffer);
                buffer.writeItemStackToBuffer(draggedStack);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        if (handlePhantomActionClient) {
            phantomClick(clickData, draggedStack);
        }
        draggedStack.stackSize = 0;
        return true;
    }

    protected void handleDragAndDropServer(ClickData clickData, ItemStack draggedStack) {
        phantomClick(clickData, draggedStack);
    }

    private GuiContainerAccessor getGuiAccessor() {
        return getContext().getScreen().getAccessor();
    }

    private ModularGui getScreen() {
        return getContext().getScreen();
    }

    @SideOnly(Side.CLIENT)
    protected void drawSlot(Slot slotIn) {
        drawSlot(slotIn, true);
    }

    /**
     * Copied from {@link net.minecraft.client.gui.inventory.GuiContainer} and removed the bad parts
     */
    @SideOnly(Side.CLIENT)
    protected void drawSlot(Slot slotIn, boolean drawStackSize) {
        int x = slotIn.xDisplayPosition;
        int y = slotIn.yDisplayPosition;
        ItemStack itemstack = slotIn.getStack();
        boolean flag = false;
        boolean flag1 = slotIn == getGuiAccessor().getClickedSlot()
                && getGuiAccessor().getDraggedStack() != null
                && !getGuiAccessor().getIsRightMouseClick();
        ItemStack itemstack1 = getContext().getPlayer().inventory.getItemStack();
        int amount = -1;
        String format = null;

        if (slotIn == this.getGuiAccessor().getClickedSlot()
                && getGuiAccessor().getDraggedStack() != null
                && getGuiAccessor().getIsRightMouseClick()
                && itemstack != null) {
            itemstack = itemstack.copy();
            itemstack.stackSize = itemstack.stackSize / 2;
        } else if (getScreen().isDragSplitting() && getScreen().getDragSlots().contains(slotIn) && itemstack1 != null) {
            if (getScreen().getDragSlots().size() == 1) {
                return;
            }

            // Container#canAddItemToSlot
            if (Container.func_94527_a(slotIn, itemstack1, true)
                    && getScreen().inventorySlots.canDragIntoSlot(slotIn)) {
                itemstack = itemstack1.copy();
                flag = true;
                // Container#computeStackSize
                Container.func_94525_a(
                        getScreen().getDragSlots(),
                        getGuiAccessor().getDragSplittingLimit(),
                        itemstack,
                        slotIn.getStack() == null ? 0 : slotIn.getStack().stackSize);
                int k = Math.min(itemstack.getMaxStackSize(), slotIn.getSlotStackLimit());

                if (itemstack.stackSize > k) {
                    amount = k;
                    format = EnumChatFormatting.YELLOW.toString();
                    itemstack.stackSize = k;
                }
            } else {
                getScreen().getDragSlots().remove(slotIn);
                getGuiAccessor().invokeUpdateDragSplitting();
            }
        }

        getScreen().setZ(100f);
        ModularGui.getItemRenderer().zLevel = 100.0F;

        if (!flag1) {
            if (flag) {
                ModularGui.drawSolidRect(1, 1, 16, 16, -2130706433);
            }

            if (itemstack != null) {
                GlStateManager.enableRescaleNormal();
                GlStateManager.enableLighting();
                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.enableDepth();
                GlStateManager.pushMatrix();
                // so that item z levels are properly ordered
                GlStateManager.translate(0, 0, 150 * getWindowLayer());
                // render the item itself
                ModularGui.getItemRenderer()
                        .renderItemAndEffectIntoGUI(
                                getScreen().getFontRenderer(),
                                Minecraft.getMinecraft().getTextureManager(),
                                itemstack,
                                1,
                                1);
                GlStateManager.popMatrix();

                if (drawStackSize) {
                    if (amount < 0) {
                        amount = itemstack.stackSize;
                    }
                    // render the amount overlay
                    if (amount > 1 || format != null) {
                        String amountText = NumberFormat.format(amount, 2);
                        if (format != null) {
                            amountText = format + amountText;
                        }
                        float scale = 1f;
                        if (amountText.length() == 3) {
                            scale = 0.8f;
                        } else if (amountText.length() == 4) {
                            scale = 0.6f;
                        } else if (amountText.length() > 4) {
                            scale = 0.5f;
                        }
                        textRenderer.setShadow(true);
                        textRenderer.setScale(scale);
                        textRenderer.setColor(Color.WHITE.normal);
                        textRenderer.setAlignment(Alignment.BottomRight, size.width - 1, size.height - 1);
                        textRenderer.setPos(1, 1);
                        GlStateManager.disableLighting();
                        GlStateManager.disableDepth();
                        GlStateManager.disableBlend();
                        textRenderer.draw(amountText);
                        GlStateManager.enableLighting();
                        GlStateManager.enableDepth();
                        GlStateManager.enableBlend();
                    }
                }

                int cachedCount = itemstack.stackSize;
                itemstack.stackSize = 1; // required to not render the amount overlay
                // render other overlays like durability bar
                ModularGui.getItemRenderer()
                        .renderItemOverlayIntoGUI(
                                getScreen().getFontRenderer(),
                                Minecraft.getMinecraft().getTextureManager(),
                                itemstack,
                                1,
                                1,
                                null);
                itemstack.stackSize = cachedCount;
                GlStateManager.disableDepth();
            }
        }

        ModularGui.getItemRenderer().zLevel = 0.0F;
        getScreen().setZ(0f);
    }
}
