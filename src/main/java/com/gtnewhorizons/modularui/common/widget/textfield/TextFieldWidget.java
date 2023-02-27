package com.gtnewhorizons.modularui.common.widget.textfield;

import java.awt.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.api.drawable.GuiHelper;
import com.gtnewhorizons.modularui.api.math.MathExpression;
import com.gtnewhorizons.modularui.api.widget.ISyncedWidget;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

/**
 * Text input widget with one line only. Can be synced between client and server. Can handle text validation.
 */
public class TextFieldWidget extends BaseTextFieldWidget implements ISyncedWidget {

    private boolean needsUpdate;

    private Supplier<String> getter;
    private Consumer<String> setter;
    private Function<String, String> validator = val -> val;
    private BiFunction<String, Integer, String> onScroll;

    private boolean syncsToServer = true;
    private boolean syncsToClient = true;

    public static Number parse(String num) {
        try {
            return format.parse(num);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onInit() {}

    @Override
    public void draw(float partialTicks) {
        Point draggableTranslate = getDraggableTranslate();
        GuiHelper
                .useScissor(pos.x + draggableTranslate.x, pos.y + draggableTranslate.y, size.width, size.height, () -> {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(1 - scrollOffset, 1, 0);
                    renderer.setSimulate(false);
                    renderer.setScale(scale);
                    renderer.setAlignment(textAlignment, scrollBar == null ? size.width - 2 : -1, size.height);
                    renderer.draw(handler.getText());
                    GlStateManager.popMatrix();
                });
    }

    @NotNull
    public String getText() {
        if (handler.getText().isEmpty()) {
            return "";
        }
        if (handler.getText().size() > 1) {
            throw new IllegalStateException("TextFieldWidget can only have one line!");
        }
        return handler.getText().get(0);
    }

    public void setText(@NotNull String text) {
        if (handler.getText().isEmpty()) {
            handler.getText().add(text);
        } else {
            handler.getText().set(0, text);
        }
    }

    @Override
    public void onRemoveFocus() {
        super.onRemoveFocus();
        if (handler.getText().size() > 1) {
            throw new IllegalStateException("TextFieldWidget can only have one line!");
        }
        setText(validator.apply(getText()));
        if (setter != null) {
            setter.accept(getText());
        }
        if (syncsToServer()) {
            syncToServer(1, buffer -> NetworkUtils.writeStringSafe(buffer, getText()));
        }
    }

    @Override
    public boolean onMouseScroll(int direction) {
        if (!isFocused()) return false;
        if (onScroll != null) {
            setText(validator.apply(onScroll.apply(getText(), direction)));
            if (setter != null) {
                setter.accept(getText());
            }
            if (syncsToServer()) {
                syncToServer(1, buffer -> NetworkUtils.writeStringSafe(buffer, getText()));
            }
            return true;
        }
        return false;
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (syncsToClient() && getter != null) {
            String val = getter.get();
            if (init || !getText().equals(val)) {
                setText(val);
                syncToClient(1, buffer -> {
                    buffer.writeBoolean(init);
                    NetworkUtils.writeStringSafe(buffer, getText());
                });
                markForUpdate();
            }
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            boolean init = buf.readBoolean();
            if (init || !isFocused()) {
                setText(NetworkUtils.readStringSafe(buf));
                if (init) {
                    lastText = new ArrayList<>(handler.getText());
                    if (focusOnGuiOpen) {
                        forceFocus();
                    }
                }
                if (this.setter != null && (this.getter == null || !getText().equals(this.getter.get()))) {
                    this.setter.accept(getText());
                }
            }
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 1) {
            setText(NetworkUtils.readStringSafe(buf));
            if (this.setter != null) {
                this.setter.accept(getText());
            }
            markForUpdate();
        }
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

    /**
     * @return if this widget should operate on the server side. For example detecting and sending changes to client.
     */
    public boolean syncsToClient() {
        return syncsToClient;
    }

    /**
     * @return if this widget should operate on the client side. For example, sending a changed value to the server.
     */
    public boolean syncsToServer() {
        return syncsToServer;
    }

    /**
     * Determines how this widget should sync values
     *
     * @param syncsToClient if this widget should sync changes to the server
     * @param syncsToServer if this widget should detect changes on server and sync them to client
     */
    public TextFieldWidget setSynced(boolean syncsToClient, boolean syncsToServer) {
        this.syncsToClient = syncsToClient;
        this.syncsToServer = syncsToServer;
        return this;
    }

    public TextFieldWidget setMaxLength(int maxLength) {
        this.handler.setMaxCharacters(maxLength);
        return this;
    }

    public TextFieldWidget setSetter(Consumer<String> setter) {
        this.setter = setter;
        return this;
    }

    public TextFieldWidget setSetterLong(Consumer<Long> setter) {
        this.setter = val -> {
            if (!val.isEmpty()) {
                setter.accept(parse(val).longValue());
            }
        };
        return this;
    }

    public TextFieldWidget setSetterInt(Consumer<Integer> setter) {
        this.setter = val -> {
            if (!val.isEmpty()) {
                setter.accept(parse(val).intValue());
            }
        };
        return this;
    }

    public TextFieldWidget setGetter(Supplier<String> getter) {
        this.getter = getter;
        return this;
    }

    public TextFieldWidget setGetterLong(Supplier<Long> getter) {
        this.getter = () -> String.valueOf(getter.get());
        return this;
    }

    public TextFieldWidget setGetterInt(Supplier<Integer> getter) {
        this.getter = () -> String.valueOf(getter.get());
        return this;
    }

    public TextFieldWidget setPattern(Pattern pattern) {
        handler.setPattern(pattern);
        return this;
    }

    public TextFieldWidget setTextColor(int textColor) {
        this.renderer.setColor(textColor);
        return this;
    }

    public TextFieldWidget setMarkedColor(int color) {
        this.renderer.setMarkedColor(color);
        return this;
    }

    public TextFieldWidget setValidator(Function<String, String> validator) {
        this.validator = validator;
        return this;
    }

    public TextFieldWidget setNumbersLong(Function<Long, Long> validator) {
        setPattern(WHOLE_NUMS);
        setValidator(val -> {
            long num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                num = (long) MathExpression.parseMathExpression(val);
            }
            return decimalFormat.format(validator.apply(num));
        });
        return this;
    }

    public TextFieldWidget setNumbers(Function<Integer, Integer> validator) {
        setPattern(WHOLE_NUMS);
        return setValidator(val -> {
            int num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                num = (int) MathExpression.parseMathExpression(val);
            }
            return decimalFormat.format(validator.apply(num));
        });
    }

    public TextFieldWidget setNumbersDouble(Function<Double, Double> validator) {
        setPattern(DECIMALS);
        return setValidator(val -> {
            double num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                num = MathExpression.parseMathExpression(val);
            }
            return decimalFormat.format(validator.apply(num));
        });
    }

    public TextFieldWidget setNumbers(Supplier<Integer> min, Supplier<Integer> max) {
        return setNumbers(val -> Math.min(max.get(), Math.max(min.get(), val)));
    }

    public TextFieldWidget setNumbersLong(Supplier<Long> min, Supplier<Long> max) {
        return setNumbersLong(val -> Math.min(max.get(), Math.max(min.get(), val)));
    }

    public TextFieldWidget setNumbers(int min, int max) {
        return setNumbers(val -> Math.min(max, Math.max(min, val)));
    }

    /**
     * (current text, direction) -> new text
     */
    public TextFieldWidget setOnScroll(BiFunction<String, Integer, String> onScroll) {
        this.onScroll = onScroll;
        return this;
    }

    /**
     * (current number, direction) -> new number
     */
    public TextFieldWidget setOnScrollNumbers(BiFunction<Integer, Integer, Integer> onScroll) {
        return setOnScroll(
                (text, direction) -> decimalFormat
                        .format(onScroll.apply((int) MathExpression.parseMathExpression(text), direction)));
    }

    public TextFieldWidget setOnScrollNumbers(int baseStep, int ctrlStep, int shiftStep) {
        return setOnScrollNumbers((val, direction) -> {
            int step = (Interactable.hasShiftDown() ? shiftStep : Interactable.hasControlDown() ? ctrlStep : baseStep)
                    * direction;
            try {
                val = Math.addExact(val, step);
            } catch (ArithmeticException ignored) {
                val = Integer.MAX_VALUE;
            }
            return val;
        });
    }

    /**
     * (current number, direction) -> new number
     */
    public TextFieldWidget setOnScrollNumbersDouble(BiFunction<Double, Integer, Double> onScroll) {
        return setOnScroll(
                (text, direction) -> decimalFormat
                        .format(onScroll.apply(MathExpression.parseMathExpression(text), direction)));
    }

    /**
     * (current number, direction) -> new number
     */
    public TextFieldWidget setOnScrollNumbersLong(BiFunction<Long, Integer, Long> onScroll) {
        return setOnScroll(
                (text, direction) -> decimalFormat
                        .format(onScroll.apply((long) MathExpression.parseMathExpression(text), direction)));
    }

    public TextFieldWidget setOnScrollNumbersLong(long baseStep, long ctrlStep, long shiftStep) {
        return setOnScrollNumbersLong((val, direction) -> {
            long step = (Interactable.hasShiftDown() ? shiftStep : Interactable.hasControlDown() ? ctrlStep : baseStep)
                    * direction;
            try {
                val = Math.addExact(val, step);
            } catch (ArithmeticException ignored) {
                val = Long.MAX_VALUE;
            }
            return val;
        });
    }
}
