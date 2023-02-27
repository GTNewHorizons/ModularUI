package com.gtnewhorizons.modularui.common.widget.textfield;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.api.KeyboardUtil;
import com.gtnewhorizons.modularui.api.drawable.GuiHelper;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.DraggableWindowWrapper;
import com.gtnewhorizons.modularui.api.widget.IDraggable;
import com.gtnewhorizons.modularui.api.widget.IWidgetParent;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.api.widget.scroll.IHorizontalScrollable;
import com.gtnewhorizons.modularui.api.widget.scroll.ScrollType;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import com.gtnewhorizons.modularui.common.widget.ScrollBar;
import com.gtnewhorizons.modularui.config.Config;

/**
 * The base of a text input widget. Handles mouse/keyboard input and rendering.
 */
public class BaseTextFieldWidget extends Widget implements IWidgetParent, Interactable, IHorizontalScrollable {

    /**
     * @deprecated Has been replaced by non-static variable. Use {@link #getDecimalFormatter()} and
     *             {@link #setDecimalFormatter(DecimalFormat)}
     */
    @Deprecated
    public static final DecimalFormat format = (DecimalFormat) NumberFormat.getInstance();

    static {
        format.setGroupingUsed(false);
        format.setMaximumFractionDigits(8);
    }

    /**
     * all positive whole numbers
     */
    public static final Pattern NATURAL_NUMS = Pattern.compile("[0-9]*([+\\-*/%^][0-9]*)*");
    /**
     * all positive and negative numbers
     */
    public static final Pattern WHOLE_NUMS = Pattern.compile("-?[0-9]*([+\\-*/%^][0-9]*)*");

    public static final Pattern DECIMALS = Pattern.compile("[0-9]*(\\.[0-9]*)?([+\\-*/%^][0-9]*(\\.[0-9]*)?)*");
    /**
     * alphabets
     */
    public static final Pattern LETTERS = Pattern.compile("[a-zA-Z]*");

    public static final Pattern ANY = Pattern.compile(".*");
    /**
     * ascii letters
     */
    public static final Pattern BASE_PATTERN = Pattern.compile("[A-Za-z0-9\\s_+\\-.,!@#$%^&*();\\\\/|<>\"'\\[\\]?=]");

    protected TextFieldHandler handler = new TextFieldHandler();
    protected List<String> lastText;
    protected TextFieldRenderer renderer = new TextFieldRenderer(handler);
    protected Alignment textAlignment = Alignment.TopLeft;
    protected int scrollOffset = 0;
    protected float scale = 1f;
    protected int cursorTimer;
    protected boolean focusOnGuiOpen;
    protected DecimalFormat decimalFormat;

    protected ScrollBar scrollBar;

    public BaseTextFieldWidget() {
        this.handler.setRenderer(renderer);
        decimalFormat = new DecimalFormat();
        decimalFormat.setGroupingUsed(true);
    }

    @Override
    public List<Widget> getChildren() {
        return scrollBar == null ? Collections.emptyList() : Collections.singletonList(scrollBar);
    }

    @Override
    public void onPostInit() {
        super.onPostInit();
        if (focusOnGuiOpen) {
            forceFocus();
        }
    }

    protected void forceFocus() {
        if (NetworkUtils.isClient()) {
            getContext().getCursor().updateFocused(this);
        }
        handler.markAll();
    }

    @Override
    public void onScreenUpdate() {
        if (isFocused() && ++cursorTimer == 10) {
            renderer.toggleCursor();
            cursorTimer = 0;
        }
    }

    @Override
    public void draw(float partialTicks) {
        Point draggableTranslate = getDraggableTranslate();
        GuiHelper
                .useScissor(pos.x + draggableTranslate.x, pos.y + draggableTranslate.y, size.width, size.height, () -> {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(1 - scrollOffset, 1, 0);
                    renderer.setSimulate(false);
                    renderer.setScale(scale);
                    renderer.setAlignment(textAlignment, -2, size.height);
                    renderer.draw(handler.getText());
                    GlStateManager.popMatrix();
                });
    }

    protected Point getDraggableTranslate() {
        Point ret = new Point();
        IDraggable draggable = getWindow().getContext().getCursor().getDraggable();
        if (!isBeingDragged(draggable)) return ret;
        Rectangle draggableArea = draggable.getArea();

        ret.translate(-getWindow().getPos().x, -getWindow().getPos().y);
        // noinspection ConstantConditions
        ret.translate(draggableArea.x, draggableArea.y);
        return ret;
    }

    private boolean isBeingDragged(IDraggable draggable) {
        if (draggable == null) return false;
        AtomicBoolean found = new AtomicBoolean(false);
        IWidgetParent parent;
        if (draggable instanceof IWidgetParent) {
            parent = (IWidgetParent) draggable;
        } else if (draggable instanceof DraggableWindowWrapper) {
            parent = ((DraggableWindowWrapper) draggable).getWindow();
        } else {
            return false;
        }
        IWidgetParent.forEachByLayer(parent, widget -> {
            if (widget == this) {
                found.set(true);
                return true;
            }
            return false;
        });
        return found.get();
    }

    @Override
    public boolean shouldGetFocus() {
        this.cursorTimer = 0;
        this.renderer.setCursor(true);
        this.lastText = new ArrayList<>(handler.getText());
        return true;
    }

    @Override
    public boolean canHover() {
        return true;
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (!isRightBelowMouse()) {
            return ClickResult.IGNORE;
        }
        handler.setCursor(
                renderer.getCursorPos(
                        handler.getText(),
                        getContext().getCursor().getX() - pos.x + scrollOffset,
                        getContext().getCursor().getY() - pos.y));
        return ClickResult.SUCCESS;
    }

    @Override
    public void onMouseDragged(int buttonId, long deltaTime) {
        handler.setMainCursor(
                renderer.getCursorPos(
                        handler.getText(),
                        getContext().getCursor().getX() - pos.x + scrollOffset,
                        getContext().getCursor().getY() - pos.y));
    }

    @Override
    public boolean onMouseScroll(int direction) {
        return scrollBar != null && this.scrollBar.onMouseScroll(direction);
    }

    @Override
    public boolean onKeyPressed(char character, int keyCode) {
        if (!isFocused()) return false;
        switch (keyCode) {
            case Keyboard.KEY_RETURN:
                if (getMaxLines() > 1) {
                    handler.newLine();
                } else {
                    removeFocus();
                }
                return true;
            case Keyboard.KEY_ESCAPE:
                if (Config.escRestoreLastText) {
                    handler.getText().clear();
                    handler.getText().addAll(lastText);
                }
                removeFocus();
                return true;
            case Keyboard.KEY_LEFT: {
                handler.moveCursorLeft(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return true;
            }
            case Keyboard.KEY_RIGHT: {
                handler.moveCursorRight(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return true;
            }
            case Keyboard.KEY_UP: {
                handler.moveCursorUp(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return true;
            }
            case Keyboard.KEY_DOWN: {
                handler.moveCursorDown(Interactable.hasControlDown(), Interactable.hasShiftDown());
                return true;
            }
            case Keyboard.KEY_DELETE:
                handler.delete(true);
                return true;
            case Keyboard.KEY_BACK:
                handler.delete();
                return true;
        }

        if (character == Character.MIN_VALUE) {
            return false;
        }

        if (KeyboardUtil.isKeyComboCtrlC(keyCode)) {
            // copy marked text
            GuiScreen.setClipboardString(handler.getSelectedText());
            return true;
        } else if (KeyboardUtil.isKeyComboCtrlV(keyCode)) {
            // paste copied text in marked text
            handler.insert(GuiScreen.getClipboardString());
            return true;
        } else if (KeyboardUtil.isKeyComboCtrlX(keyCode) && handler.hasTextMarked()) {
            // copy and delete copied text
            GuiScreen.setClipboardString(handler.getSelectedText());
            handler.delete();
            return true;
        } else if (KeyboardUtil.isKeyComboCtrlA(keyCode)) {
            // mark whole text
            handler.markAll();
            return true;
        } else if (BASE_PATTERN.matcher(String.valueOf(character)).matches()) {
            if (handler.test(String.valueOf(character))) {
                // delete selected chars
                if (handler.hasTextMarked()) {
                    handler.delete();
                }
                // insert typed char
                handler.insert(String.valueOf(character));
                return true;
            }
        }
        if (Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode() == keyCode) {
            removeFocus();
            return true;
        }
        return true;
    }

    @Override
    public void onRemoveFocus() {
        super.onRemoveFocus();
        this.renderer.setCursor(false);
        this.cursorTimer = 0;
        this.scrollOffset = 0;
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return new Size(maxWidth, (int) (renderer.getFontHeight() * getMaxLines() + 0.5));
    }

    @Override
    public void setHorizontalScrollOffset(int offset) {
        if (this.scrollBar != null && this.scrollBar.isActive()) {
            this.scrollOffset = offset;
        } else {
            this.scrollOffset = 0;
        }
    }

    @Override
    public int getHorizontalScrollOffset() {
        return this.scrollOffset;
    }

    @Override
    public int getVisibleWidth() {
        return size.width;
    }

    @Override
    public int getActualWidth() {
        return (int) Math.ceil(renderer.getLastWidth());
    }

    public int getMaxLines() {
        return handler.getMaxLines();
    }

    public List<String> getLastText() {
        return lastText;
    }

    public BaseTextFieldWidget setTextAlignment(Alignment textAlignment) {
        this.textAlignment = textAlignment;
        return this;
    }

    public BaseTextFieldWidget setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public BaseTextFieldWidget setScrollBar() {
        return setScrollBar(0);
    }

    public BaseTextFieldWidget setScrollBar(int posOffset) {
        return setScrollBar(ScrollBar.defaultTextScrollBar().setPosOffset(posOffset));
    }

    public BaseTextFieldWidget setScrollBar(@Nullable ScrollBar scrollBar) {
        this.scrollBar = scrollBar;
        this.handler.setScrollBar(scrollBar);
        if (this.scrollBar != null) {
            this.scrollBar.setScrollType(ScrollType.HORIZONTAL, this, null);
        }
        return this;
    }

    public BaseTextFieldWidget setTextColor(int color) {
        this.renderer.setColor(color);
        return this;
    }

    public BaseTextFieldWidget setFocusOnGuiOpen(boolean focused) {
        focusOnGuiOpen = focused;
        return this;
    }

    public BaseTextFieldWidget setMaximumFractionDigits(int digits) {
        decimalFormat.setMaximumFractionDigits(digits);
        return this;
    }

    public BaseTextFieldWidget setDecimalFormatter(@NotNull DecimalFormat decimalFormat) {
        this.decimalFormat = decimalFormat;
        return this;
    }

    public DecimalFormat getDecimalFormatter() {
        return decimalFormat;
    }

    public static char getDecimalSeparator() {
        return format.getDecimalFormatSymbols().getDecimalSeparator();
    }

    public static char getGroupSeparator() {
        return format.getDecimalFormatSymbols().getGroupingSeparator();
    }
}
