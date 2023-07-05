package com.gtnewhorizons.modularui.common.internal.wrapper;

import static codechicken.lib.render.FontUtils.fontRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.api.drawable.GuiHelper;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.Cursor;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.widget.IDragAndDropHandler;
import com.gtnewhorizons.modularui.api.widget.IHasStackUnderMouse;
import com.gtnewhorizons.modularui.api.widget.IVanillaSlot;
import com.gtnewhorizons.modularui.api.widget.IWidgetParent;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.config.Config;
import com.gtnewhorizons.modularui.mixins.GuiContainerAccessor;

import codechicken.nei.ItemPanels;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerDrawHandler;
import codechicken.nei.guihook.IContainerInputHandler;
import codechicken.nei.guihook.IContainerObjectHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModularGui extends GuiContainer implements INEIGuiHandler {

    private final ModularUIContext context;
    private Pos2d mousePos = Pos2d.ZERO;

    @Nullable
    private Object lastClicked;

    private long lastClick = -1;
    private long lastFocusedClick = -1;
    private int drawCalls = 0;
    private long drawTime = 0;
    private int fps = 0;

    private float partialTicks;

    public ModularGui(ModularUIContainer container) {
        super(container);
        this.context = container.getContext();
        this.context.initializeClient(this);
    }

    public ModularUIContext getContext() {
        return context;
    }

    public Cursor getCursor() {
        return context.getCursor();
    }

    public Pos2d getMousePos() {
        return mousePos;
    }

    // @Override
    // public void onResize(@NotNull Minecraft mc, int w, int h) {
    // super.onResize(mc, w, h);
    // context.resize(new Size(w, h));
    // }

    public void setMainWindowArea(Pos2d pos, Size size) {
        this.guiLeft = pos.x;
        this.guiTop = pos.y;
        this.xSize = size.width;
        this.ySize = size.height;
    }

    @Override
    public void initGui() {
        super.initGui();
        context.resize(new Size(width, height));
        this.context.buildWindowOnStart();
        this.context.getCurrentWindow().onOpen();
    }

    public GuiContainerAccessor getAccessor() {
        return (GuiContainerAccessor) this;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mousePos = new Pos2d(mouseX, mouseY);

        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        // mainly for invtweaks compat
        drawVanillaElements(mouseX, mouseY, partialTicks);
        GlStateManager.pushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        getAccessor().setHoveredSlot(null);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.enableGUIStandardItemLighting();
        if (shouldShowNEI()) {
            // Copied from GuiContainerManager#renderObjects but without translation
            for (IContainerDrawHandler drawHandler : GuiContainerManager.drawHandlers) {
                drawHandler.renderObjects(this, mouseX, mouseY);
            }
            for (IContainerDrawHandler drawHandler : GuiContainerManager.drawHandlers) {
                drawHandler.postRenderObjects(this, mouseX, mouseY);
            }

            if (!shouldRenderOurTooltip() && shouldRenderNEITooltip() && GuiContainerManager.getManager() != null) {
                GuiContainerManager.getManager().renderToolTips(mouseX, mouseY);
            }
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        RenderHelper.enableGUIStandardItemLighting();

        getAccessor().setHoveredSlot(null);
        Widget hovered = getCursor().getHovered();
        if (hovered instanceof IVanillaSlot) {
            getAccessor().setHoveredSlot(((IVanillaSlot) hovered).getMcSlot());
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(i, j, 0);
        GlStateManager.popMatrix();

        InventoryPlayer inventoryplayer = this.mc.thePlayer.inventory;
        ItemStack itemstack = getAccessor().getDraggedStack() == null ? inventoryplayer.getItemStack()
                : getAccessor().getDraggedStack();
        GlStateManager.translate((float) i, (float) j, 0.0F);
        if (itemstack != null) {
            int k2 = getAccessor().getDraggedStack() == null ? 8 : 16;
            String s = null;

            if (getAccessor().getDraggedStack() != null && getAccessor().getIsRightMouseClick()) {
                itemstack = itemstack.copy();
                itemstack.stackSize = (int) Math.ceil((float) itemstack.stackSize / 2.0F);
            } else if (this.isDragSplitting() && this.getDragSlots().size() > 1) {
                itemstack = itemstack.copy();
                itemstack.stackSize = getAccessor().getDragSplittingRemnant();

                if (itemstack.stackSize < 1) {
                    s = EnumChatFormatting.YELLOW + "0";
                }
            }

            this.drawItemStack(itemstack, mouseX - i - 8, mouseY - j - k2, s);
        }

        if (getAccessor().getReturningStack() != null) {
            float f = (float) (Minecraft.getSystemTime() - getAccessor().getReturningStackTime()) / 100.0F;

            if (f >= 1.0F) {
                f = 1.0F;
                getAccessor().setReturningStack(null);
            }

            int l2 = getAccessor().getReturningStackDestSlot().xDisplayPosition - getAccessor().getTouchUpX();
            int i3 = getAccessor().getReturningStackDestSlot().yDisplayPosition - getAccessor().getTouchUpY();
            int l1 = getAccessor().getTouchUpX() + (int) ((float) l2 * f);
            int i2 = getAccessor().getTouchUpY() + (int) ((float) i3 * f);
            this.drawItemStack(getAccessor().getReturningStack(), l1, i2, null);
        }

        GlStateManager.popMatrix();

        if (Config.debug) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            drawDebugScreen();
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableStandardItemLighting();
    }

    private void drawItemStack(ItemStack stack, int x, int y, String altText) {
        GlStateManager.translate(0.0F, 0.0F, 32.0F);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        this.zLevel = 200.0F;
        itemRender.zLevel = 200.0F;
        FontRenderer font = GuiHelper.getFontRenderer(stack);
        itemRender.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), stack, x, y);
        itemRender.renderItemOverlayIntoGUI(
                font,
                mc.getTextureManager(),
                stack,
                x,
                y - (getDragSlots() != null ? 0 : 8),
                altText);
        GuiHelper.afterRenderItemAndEffectIntoGUI(stack);
        this.zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (Config.debug) {
            long time = Minecraft.getSystemTime() / 1000;
            if (drawTime != time) {
                fps = drawCalls;
                drawCalls = 0;
                drawTime = time;
            }
            drawCalls++;
        }
        context.forEachWindowBottomToTop(window -> window.frameUpdate(partialTicks));
        drawDefaultBackground();

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        for (ModularWindow window : context.getOpenWindowsReversed()) {
            if (window.isEnabled()) {
                window.drawWidgets(partialTicks, false);
            }
        }

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_LIGHTING);
        RenderHelper.enableStandardItemLighting();

        this.partialTicks = partialTicks;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Widget hovered = context.getCursor().getHovered();
        if (shouldRenderOurTooltip()) {
            if (hovered instanceof IVanillaSlot && ((IVanillaSlot) hovered).getMcSlot().getHasStack()
                    && !context.getCursor().isHoldingSomething()) {
                renderToolTip(
                        ((IVanillaSlot) hovered).getMcSlot().getStack(),
                        mouseX,
                        mouseY,
                        ((IVanillaSlot) hovered).getExtraTooltip(),
                        ((IVanillaSlot) hovered).getOverwriteItemStackTooltip());
            } else if (hovered.getTooltipShowUpDelay() <= context.getCursor().getTimeHovered()) {
                List<Text> tooltip = new ArrayList<>(hovered.getTooltip()); // avoid UOE
                if (hovered.hasNEITransferRect()) {
                    String transferRectTooltip = hovered.getNEITransferRectTooltip();
                    if (transferRectTooltip != null) {
                        tooltip.add(new Text(transferRectTooltip).color(Color.WHITE.normal));
                    }
                }
                if (!tooltip.isEmpty()) {
                    GuiHelper.drawHoveringText(
                            tooltip,
                            context.getMousePos(),
                            context.getScaledScreenSize(),
                            400,
                            1,
                            false,
                            Alignment.CenterLeft,
                            hovered.isTooltipHasSpaceAfterFirstLine());
                }
            }
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }

        if (context.getCurrentWindow().isEnabled()) {
            context.getCurrentWindow().drawWidgets(partialTicks, true);
        }
        context.getCursor().draw(partialTicks);

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_LIGHTING);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    /**
     * @return False if NEI wants to draw their own tooltip e.g. ItemPanel
     */
    protected boolean shouldRenderOurTooltip() {
        return context.getCursor().getHovered() != null;
    }

    protected boolean shouldRenderNEITooltip() {
        // taken from GuiContainerManager#getStackMouseOver but don't check #getSlotMouseOver
        // as it sees our slot even if it's disabled
        for (IContainerObjectHandler objectHandler : GuiContainerManager.objectHandlers) {
            ItemStack item = objectHandler
                    .getStackUnderMouse(this, context.getCursor().getPos().x, context.getCursor().getPos().y);
            if (item != null) return true;
        }
        return false;
    }

    public void drawDebugScreen() {
        Size screenSize = context.getScaledScreenSize();
        int neiYOffset = shouldShowNEI() ? 20 : 0;
        int color = Color.rgb(180, 40, 115);
        int lineY = screenSize.height - 13 - neiYOffset;
        drawString(fontRenderer, "Mouse Pos: " + getMousePos(), 5, lineY, color);
        lineY -= 11;
        drawString(fontRenderer, "FPS: " + fps, 5, screenSize.height - 24 - neiYOffset, color);
        lineY -= 11;
        Widget hovered = context.getCursor().findHoveredWidget(true);
        if (hovered != null) {
            Size size = hovered.getSize();
            Pos2d pos = hovered.getAbsolutePos();
            IWidgetParent parent = hovered.getParent();

            drawBorder(pos.x, pos.y, size.width, size.height, color, 1f);
            drawBorder(
                    parent.getAbsolutePos().x,
                    parent.getAbsolutePos().y,
                    parent.getSize().width,
                    parent.getSize().height,
                    Color.withAlpha(color, 0.3f),
                    1f);
            drawText("Pos: " + hovered.getPos(), 5, lineY, 1, color, false);
            lineY -= 11;
            drawText("Size: " + size, 5, lineY, 1, color, false);
            lineY -= 11;
            drawText(
                    "Parent: " + (parent instanceof ModularWindow ? "ModularWindow" : parent.toString()),
                    5,
                    lineY,
                    1,
                    color,
                    false);
            lineY -= 11;
            drawText("Class: " + hovered, 5, lineY, 1, color, false);
            lineY -= 11;
            if (hovered instanceof SlotWidget) {
                BaseSlot slot = ((SlotWidget) hovered).getMcSlot();
                drawText("Slot Index: " + slot.getSlotIndex(), 5, lineY, 1, color, false);
                lineY -= 11;
                drawText("Slot Number: " + slot.slotNumber, 5, lineY, 1, color, false);
                lineY -= 11;
                drawText(
                        "Shift-Click Priority: "
                                + (slot.getShiftClickPriority() != Integer.MIN_VALUE ? slot.getShiftClickPriority()
                                        : "DISABLED"),
                        5,
                        lineY,
                        1,
                        color,
                        false);
            }
        }
        color = Color.withAlpha(color, 25);
        for (int i = 5; i < screenSize.width; i += 5) {
            drawVerticalLine(i, 0, screenSize.height, color);
        }

        for (int i = 5; i < screenSize.height; i += 5) {
            drawHorizontalLine(0, screenSize.width, i, color);
        }
        drawRect(mousePos.x, mousePos.y, mousePos.x + 1, mousePos.y + 1, Color.withAlpha(Color.GREEN.normal, 0.8f));
    }

    protected void renderToolTip(ItemStack stack, int x, int y, List<String> extraLines,
            Function<List<String>, List<String>> overwriteItemStackTooltip) {
        List<String> lines = new ArrayList<>();
        if (stack != null) {
            lines.addAll(GuiHelper.getItemTooltip(stack));
        }
        lines.addAll(extraLines);
        if (overwriteItemStackTooltip != null) {
            lines = overwriteItemStackTooltip.apply(lines);
        }
        // see GuiContainerManager#renderToolTips for these magic numbers
        GuiContainerManager.drawPagedTooltip(GuiHelper.getFontRenderer(stack), x + 12, y - 12, lines);
    }

    protected void drawVanillaElements(int mouseX, int mouseY, float partialTicks) {
        for (Object guiButton : this.buttonList) {
            ((GuiButton) guiButton).drawButton(this.mc, mouseX, mouseY);
        }
        for (Object guiLabel : this.labelList) {
            ((GuiLabel) guiLabel).func_146159_a(this.mc, mouseX, mouseY);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        context.onClientTick();
        for (ModularWindow window : context.getOpenWindowsReversed()) {
            window.update();
        }
        context.getCursor().updateHovered();
        context.getCursor().onScreenUpdate();
    }

    private boolean isDoubleClick(long lastClick, long currentClick) {
        return currentClick - lastClick < 500;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        long time = Minecraft.getSystemTime();
        boolean doubleClick = isDoubleClick(lastClick, time);
        lastClick = time;
        // For reference: in 1.12 JEI handles drag-and-drop on MouseInputEvent.Pre event,
        // which is fired before GuiScreen#handleMouseInput call and able to dismiss it.
        // In contrast, NEI injects GuiContainerManager#mouseClicked at the start of GuiContainer#mouseClicked,
        // so at this point NEI has not handled drag-and-drop yet.
        // See also: PanelWidget#handleClickExt

        for (Interactable interactable : context.getCurrentWindow().getInteractionListeners()) {
            if (shouldSkipClick(interactable)) continue;
            interactable.onClick(mouseButton, doubleClick);
        }

        if (context.getCursor().onMouseClick(mouseButton)) {
            lastFocusedClick = time;
            return;
        }

        Object probablyClicked = null;
        boolean wasSuccess = false;
        boolean wasReject = false;
        doubleClick = isDoubleClick(lastFocusedClick, time);
        loop: for (Object hovered : getCursor().getAllHovered()) {
            if (shouldSkipClick(hovered)) break;
            if (context.getCursor().onHoveredClick(mouseButton, hovered)) {
                probablyClicked = hovered;
                break;
            }
            if (hovered instanceof ModularWindow) {
                // if floating window is clicked (while holding item), widgets/slots below should not be interacted
                probablyClicked = hovered;
                wasReject = true;
                break;
            }
            if (hovered instanceof Widget) {
                Widget widget = (Widget) hovered;
                if (widget.hasNEITransferRect()) {
                    if (mouseButton == 0) {
                        widget.handleTransferRectMouseClick(false);
                    } else if (mouseButton == 1) {
                        widget.handleTransferRectMouseClick(true);
                    }
                    probablyClicked = hovered;
                    break;
                }
            }
            if (hovered instanceof Interactable) {
                Interactable interactable = (Interactable) hovered;
                Interactable.ClickResult result = interactable
                        .onClick(mouseButton, doubleClick && lastClicked == interactable);
                switch (result) {
                    case IGNORE:
                        continue;
                    case ACKNOWLEDGED:
                        if (probablyClicked == null) {
                            probablyClicked = interactable;
                        }
                        continue;
                    case REJECT:
                        probablyClicked = null;
                        wasReject = true;
                        break loop;
                    case DELEGATE:
                        probablyClicked = null;
                        break loop;
                    case ACCEPT:
                        probablyClicked = interactable;
                        break loop;
                    case SUCCESS:
                        probablyClicked = interactable;
                        wasSuccess = true;
                        getCursor().updateFocused((Widget) interactable);
                        break loop;
                }
            }
        }
        this.lastClicked = probablyClicked;
        if (!wasSuccess) {
            getCursor().updateFocused(null);
        }
        if (probablyClicked == null && !wasReject) {
            // NEI injects GuiContainerManager#mouseClicked there
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } else {
            if (shouldShowNEI()) {
                for (IContainerInputHandler inputhander : GuiContainerManager.inputHandlers) {
                    inputhander.onMouseClicked(this, mouseX, mouseY, mouseButton);
                }
            }
        }

        lastFocusedClick = time;
    }

    private boolean isNEIWantToHandleDragAndDrop() {
        return shouldShowNEI()
                && (ItemPanels.itemPanel.draggedStack != null || ItemPanels.bookmarkPanel.draggedStack != null);
    }

    private boolean shouldSkipClick(Object object) {
        return isNEIWantToHandleDragAndDrop() && object instanceof IDragAndDropHandler;
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int mouseButton) {
        for (Interactable interactable : context.getCurrentWindow().getInteractionListeners()) {
            interactable.onClickReleased(mouseButton);
        }
        if (!context.getCursor().onMouseReleased(mouseButton) && (lastClicked == null
                || (lastClicked instanceof Interactable && !((Interactable) lastClicked).onClickReleased(mouseButton)))
                && !(lastClicked instanceof ModularWindow)) {
            super.mouseMovedOrUp(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
        for (Interactable interactable : context.getCurrentWindow().getInteractionListeners()) {
            interactable.onMouseDragged(mouseButton, timeSinceLastClick);
        }
        if (lastClicked != null && lastClicked instanceof Interactable) {
            ((Interactable) lastClicked).onMouseDragged(mouseButton, timeSinceLastClick);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        // debug mode C + CTRL + SHIFT
        if (keyCode == Keyboard.KEY_C && isCtrlKeyDown() && isShiftKeyDown()) {
            Config.debug = !Config.debug;
        }
        for (Interactable interactable : context.getCurrentWindow().getInteractionListeners()) {
            interactable.onKeyPressed(typedChar, keyCode);
        }

        Widget focused = getCursor().getFocused();
        if (focused instanceof Interactable && ((Interactable) focused).onKeyPressed(typedChar, keyCode)) {
            return;
        }
        boolean skipSuper = false;
        for (Object hovered : getCursor().getAllHovered()) {
            if (hovered instanceof ModularWindow && hovered != getContext().getMainWindow()) {
                // if popup window is present, widgets/slots below should not be interacted
                skipSuper = true;
                break;
            }
            if (focused != hovered && hovered instanceof Interactable
                    && ((Interactable) hovered).onKeyPressed(typedChar, keyCode)) {
                return;
            }
            if (hovered instanceof SlotWidget || hovered instanceof IHasStackUnderMouse) {
                // delegate to NEI keybind
                // todo: make onKeyPressed return enum and properly handle delegation
                break;
            }
        }

        if (keyCode == Keyboard.KEY_ESCAPE || this.mc.gameSettings.keyBindInventory.getKeyCode() == keyCode) {
            if (Config.closeWindowsAtOnce) {
                this.context.tryClose();
            } else {
                for (ModularWindow window : this.context.getOpenWindows()) {
                    if (!window.isClientOnly()) {
                        this.context.sendClientPacket(
                                ModularUIContext.DataCodes.CLOSE_WINDOW,
                                null,
                                window,
                                NetworkUtils.EMPTY_PACKET);
                    }
                    window.tryClose();
                    break;
                }
            }
        } else if (!skipSuper) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    /**
     * Called when mouse is scrolled, after {@link #onMouseScrolled} being called.
     */
    public boolean mouseScrolled(int direction) {
        Widget focused = getCursor().getFocused();
        if (focused instanceof Interactable && ((Interactable) focused).onMouseScroll(direction)) {
            return true;
        }
        boolean foundFirstElement = false;
        for (Object hovered : getCursor().getAllHovered()) {
            if (!foundFirstElement && hovered instanceof ModularWindow) {
                // if popup window is present, widgets/slots below should not be interacted
                return true;
            }
            if (focused != hovered && hovered instanceof Interactable
                    && ((Interactable) hovered).onMouseScroll(direction)) {
                return true;
            }
            foundFirstElement = true;
        }
        return false;
    }

    /**
     * This version of mouseScrolled is passive and will be called on every input handler before mouseScrolled is
     * processed.
     */
    public void onMouseScrolled(int direction) {
        for (Interactable interactable : context.getCurrentWindow().getInteractionListeners()) {
            interactable.onMouseScroll(direction);
        }
    }

    /**
     * This somehow overrides {@link GuiContainer#getSlotAtPosition}
     */
    @SuppressWarnings("unused")
    public Slot getSlotAtPosition(int x, int y) {
        for (Object hovered : getCursor().getAllHovered()) {
            if (hovered instanceof SlotWidget) {
                return ((SlotWidget) hovered).getMcSlot();
            }
        }
        return null;
    }

    @Override
    protected boolean checkHotbarKeys(int keyCode) {
        Widget hovered = getCursor().getHovered();
        if (hovered instanceof SlotWidget
                && ((SlotWidget) hovered).getMcSlot().getParentWidget().getWindow().isClientOnly()) {
            return false;
        }
        return super.checkHotbarKeys(keyCode);
    }

    @Override
    public void onGuiClosed() {
        context.getCloseListeners().forEach(Runnable::run);
    }

    public boolean isDragSplitting() {
        return getAccessor().isDragSplittingInternal();
    }

    public Set<Slot> getDragSlots() {
        return getAccessor().getDragSplittingSlots();
    }

    public static RenderItem getItemRenderer() {
        return itemRender;
    }

    public static void setItemRenderer(RenderItem renderer) {
        itemRender = renderer;
    }

    public float getZ() {
        return zLevel;
    }

    public void setZ(float z) {
        this.zLevel = z;
    }

    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    @SideOnly(Side.CLIENT)
    public static void drawBorder(float x, float y, float width, float height, int color, float border) {
        drawSolidRect(x - border, y - border, width + 2 * border, border, color);
        drawSolidRect(x - border, y + height, width + 2 * border, border, color);
        drawSolidRect(x - border, y, border, height, color);
        drawSolidRect(x + width, y, border, height, color);
    }

    @SideOnly(Side.CLIENT)
    public static void drawSolidRect(float x, float y, float width, float height, int color) {
        drawRect(x, y, x + width, y + height, color);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glEnable(GL11.GL_BLEND);
    }

    @SideOnly(Side.CLIENT)
    public static void drawText(String text, float x, float y, float scale, int color, boolean shadow) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 0f);
        float sf = 1 / scale;
        fontRenderer.drawString(text, (int) (x * sf), (int) (y * sf), color, shadow);
        GlStateManager.popMatrix();
        GL11.glEnable(GL11.GL_BLEND);
    }

    public static void drawRect(float left, float top, float right, float bottom, int color) {
        if (left < right) {
            float i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            float j = top;
            top = bottom;
            bottom = j;
        }

        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.instance;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glColor4f(r, g, b, a);
        tessellator.startDrawingQuads();
        // bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        tessellator.addVertex(left, bottom, 0.0D);
        tessellator.addVertex(right, bottom, 0.0D);
        tessellator.addVertex(right, top, 0.0D);
        tessellator.addVertex(left, top, 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private boolean shouldShowNEI() {
        return getContext().doShowNEI();
    }

    // === NEI ===

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button) {
        if (!(gui instanceof ModularGui) || NEIClientUtils.getHeldItem() != null) return false;
        Widget hovered = getContext().getCursor().getHovered();
        if (hovered instanceof IDragAndDropHandler) {
            return ((IDragAndDropHandler) hovered).handleDragAndDrop(draggedStack, button);
        }
        return false;
    }

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        if (!(gui instanceof ModularGui)) return false;
        Rectangle neiSlotRectangle = new Rectangle(x, y, w, h);

        for (ModularWindow window : getContext().getOpenWindows()) {
            if (window.getRectangle().intersects(neiSlotRectangle)) {
                return true;
            }
        }

        List<Widget> activeWidgets = new ArrayList<>();
        for (ModularWindow window : getContext().getOpenWindows()) {
            IWidgetParent.forEachByLayer(
                    window,
                    true,
                    // skip children search if parent does not respect NEI area
                    widget -> !widget.isRespectNEIArea(),
                    widget -> {
                        if (widget.isRespectNEIArea()) {
                            activeWidgets.add(widget);
                        }
                        return false;
                    });
        }
        for (Widget widget : activeWidgets) {
            Rectangle widgetAbsoluteRectangle = widget.getRenderAbsoluteRectangle();
            if (widgetAbsoluteRectangle.intersects(neiSlotRectangle)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility) {
        return currentVisibility;
    }

    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item) {
        return Collections.emptyList();
    }

    @Override
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
        return null;
    }
}
