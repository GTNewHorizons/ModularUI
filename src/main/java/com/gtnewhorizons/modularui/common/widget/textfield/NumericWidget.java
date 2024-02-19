package com.gtnewhorizons.modularui.common.widget.textfield;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import net.minecraft.network.PacketBuffer;

import com.gtnewhorizon.gtnhlib.util.parsing.MathExpressionParser;
import com.gtnewhorizon.gtnhlib.util.parsing.MathExpressionParser.Context;
import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.widget.ISyncedWidget;
import com.gtnewhorizons.modularui.api.widget.Interactable;

/**
 * A widget that allows the user to enter a numeric value. Synced between client and server. Automatically handles
 * number parsing and formatting. Only the numeric value (of type <code>double</code>) is exposed to the calling code.
 * <p>
 * If GTNHLib is present, also allows entering values as mathematical expressions.
 */
public class NumericWidget extends BaseTextFieldWidget implements ISyncedWidget {

    private double value = 0;
    private Supplier<Double> getter;
    private Consumer<Double> setter;
    private Function<Double, Double> validator;

    private double minValue = 0;
    private double maxValue = Double.POSITIVE_INFINITY;
    private double defaultValue = 0;
    private double scrollStep = 1;
    private double scrollStepCtrl = 0.1;
    private double scrollStepShift = 100;
    private boolean integerOnly = true;
    private Context ctx;
    private NumberFormat numberFormat;
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9.,  _]*");

    public NumericWidget() {
        setTextAlignment(Alignment.CenterLeft);
        handler.setMaxLines(1);

        // TODO: handle localization into the player's system locale.
        // This needs to be configurable in the config, if a player wants to force the US (or any other) locale despite
        // their system settings.
        numberFormat = DecimalFormat.getNumberInstance(Locale.US);

        // If you need more than 4 decimal digits of precision, use getNumberFormat().setMaximumFractionDigits().
        numberFormat.setMaximumFractionDigits(4);

        if (ModularUI.isGTNHLibLoaded) {
            handler.setPattern(MathExpressionParser.EXPRESSION_PATTERN);
            ctx = new MathExpressionParser.Context();
            ctx.setNumberFormat(numberFormat);
        } else {
            handler.setPattern(NUMBER_PATTERN);
        }
    }

    public double getValue() {
        return value;
    }

    public void setValue(double newValue) {
        value = newValue;
        if (handler.getText().isEmpty()) {
            handler.getText().add(numberFormat.format(value));
        } else {
            handler.getText().set(0, numberFormat.format(value));
        }
    }

    /**
     * @return true if the value has changed.
     */
    private boolean validateAndSetValue(double newValue) {
        newValue = Math.max(newValue, minValue);
        newValue = Math.min(newValue, maxValue);
        if (integerOnly) {
            newValue = Math.round(newValue);
        }
        if (validator != null) {
            newValue = validator.apply(newValue);
        }

        // We want to call setValue even if the value has not changed.
        // The text field might contain an expression which evaluates to the old value,
        // we still want to replace this expression with an actual number.
        boolean changed = newValue != value;
        setValue(newValue);
        return changed;
    }

    private double parseValueFromTextField() {
        if (handler.getText().isEmpty()) {
            handler.getText().add("");
        }
        if (handler.getText().size() > 1) {
            throw new IllegalStateException("NumericWidget can only have one line!");
        }

        if (ModularUI.isGTNHLibLoaded) {
            double newValue = MathExpressionParser.parse(handler.getText().get(0), ctx);
            return ctx.wasSuccessful() ? newValue : value;
        } else {
            if (handler.getText().get(0) == null || handler.getText().get(0).isEmpty()) {
                return defaultValue;
            }
            try {
                return numberFormat.parse(handler.getText().get(0)).doubleValue();
            } catch (ParseException ignore) {
                return value;
            }
        }
    }

    /* Configure widget properties. */

    /**
     * Sets the minimum allowed input value. Can be negative.
     * <p>
     * Default: 0
     */
    public NumericWidget setMinValue(double minValue) {
        this.minValue = minValue;
        return this;
    }

    /**
     * Sets the maximum allowed input value. This is also used to evaluate expressions that refer to a percentage of the
     * maximum.
     * <p>
     * Default: Double.POSITIVE_INFINITY.
     */
    public NumericWidget setMaxValue(double maxValue) {
        this.maxValue = maxValue;
        if (ModularUI.isGTNHLibLoaded) {
            ctx.setHundredPercent(maxValue);
        }
        return this;
    }

    /**
     * Sets the default input value to be used when the text field is empty.
     * <p>
     * Default: 0.
     */
    public NumericWidget setDefaultValue(double defaultValue) {
        this.defaultValue = defaultValue;
        if (ModularUI.isGTNHLibLoaded) {
            ctx.setDefaultValue(defaultValue);
        }
        return this;
    }

    /**
     * Sets the values by which to increment the value when the player uses the scroll wheel. Scrolling up increases the
     * value, scrolling down decreases. The typical convention is for ctrl to be a smaller step than the base, and shift
     * to be a larger step; but this can be changed if there is a good reason.
     * <p>
     * Default values: 1, 0.1, 100 in order.
     *
     * @param baseStep  By how much to change the value when no modifier key is held.
     * @param ctrlStep  By how much to change the value when the ctrl key is held.
     * @param shiftStep By how much to change the value when the shift key is held.
     */
    public NumericWidget setScrollValues(double baseStep, double ctrlStep, double shiftStep) {
        this.scrollStep = baseStep;
        this.scrollStepCtrl = ctrlStep;
        this.scrollStepShift = shiftStep;
        return this;
    }

    /**
     * If this is set to true, the widget will always round the entered value to the nearest integer. Otherwise, the
     * value is returned with full precision.
     * <p>
     * Default: true.
     */
    public NumericWidget setIntegerOnly(boolean integerOnly) {
        this.integerOnly = integerOnly;
        return this;
    }

    /**
     * If this is set to true, the widget will only accept a single number, and will not try to evaluate mathematical
     * expressions. Note that expression parsing requires GTNHLib.
     * <p>
     * Default: false.
     */
    public NumericWidget setPlainOnly(boolean plainOnly) {
        if (ModularUI.isGTNHLibLoaded) {
            ctx.setPlainOnly(plainOnly);
            handler.setPattern(plainOnly ? NUMBER_PATTERN : MathExpressionParser.EXPRESSION_PATTERN);
        }
        return this;
    }

    /**
     * Returns the {@link NumberFormat} used by this widget. This is a more direct method of modifying this format, such
     * as number of decimal spaces, than calling {@link #setNumberFormat(NumberFormat)} with a completely new format.
     */
    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    /**
     * Sets a {@link NumberFormat} to be used for formatting the value in the input field. Modifying the formatter
     * returned from {@link #getNumberFormat()} should be sufficient in most cases, call this only when you need to use
     * a completely different formatter.
     */
    public NumericWidget setNumberFormat(NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
        if (ModularUI.isGTNHLibLoaded) {
            ctx.setNumberFormat(numberFormat);
        }
        return this;
    }

    /**
     * Sets a supplier of numeric values to display in the input field.
     */
    public NumericWidget setGetter(Supplier<Double> getter) {
        this.getter = getter;
        return this;
    }

    /**
     * Sets a consumer of values entered by the player.
     */
    public NumericWidget setSetter(Consumer<Double> setter) {
        this.setter = setter;
        return this;
    }

    /**
     * Sets a validator for entered values. For simply restricting the value to a certain range, use
     * {@link #setMinValue(double)} and {@link #setMaxValue(double)}.
     */
    public NumericWidget setValidator(Function<Double, Double> validator) {
        this.validator = validator;
        return this;
    }

    /* Event handlers. */

    @Override
    public void onRemoveFocus() {
        super.onRemoveFocus();

        double newValue = parseValueFromTextField();
        if (validateAndSetValue(newValue)) {
            if (setter != null) {
                setter.accept(value);
            }
            if (syncsToServer()) {
                syncToServer(1, buffer -> buffer.writeDouble(value));
            }
        }
    }

    @Override
    public boolean onMouseScroll(int direction) {
        if (!isFocused()) return false;

        double newValue = parseValueFromTextField();

        if (Interactable.hasControlDown()) newValue += direction * scrollStepCtrl;
        else if (Interactable.hasShiftDown()) newValue += direction * scrollStepShift;
        else newValue += direction * scrollStep;

        if (validateAndSetValue(newValue)) {
            if (setter != null) {
                setter.accept(value);
            }
            if (syncsToServer()) {
                syncToServer(1, buffer -> buffer.writeDouble(value));
            }
        }
        return true;
    }

    /* ISyncedWidget implementation. */

    private boolean needsUpdate;
    private boolean syncsToServer = true;
    private boolean syncsToClient = true;

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
    public NumericWidget setSynced(boolean syncsToClient, boolean syncsToServer) {
        this.syncsToClient = syncsToClient;
        this.syncsToServer = syncsToServer;
        return this;
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (syncsToClient() && getter != null) {
            double newValue = getter.get();

            // Order matters here, validateAndSetValue() has side effects, it needs to be evaluated first so that it
            // does not get short-circuited.
            if (validateAndSetValue(newValue) || init) {
                syncToClient(1, buffer -> {
                    buffer.writeBoolean(init);
                    buffer.writeDouble(value);
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
                validateAndSetValue(buf.readDouble());
                if (init) {
                    lastText = new ArrayList<>(handler.getText());
                    if (focusOnGuiOpen) {
                        forceFocus();
                    }
                }
                if (this.setter != null && (this.getter == null || this.getter.get() != value)) {
                    this.setter.accept(value);
                }
            }
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 1) {
            if (validateAndSetValue(buf.readDouble())) {
                if (this.setter != null) {
                    this.setter.accept(value);
                }
                markForUpdate();
            }
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

}
