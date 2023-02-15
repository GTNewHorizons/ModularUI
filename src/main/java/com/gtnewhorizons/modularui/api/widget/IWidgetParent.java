package com.gtnewhorizons.modularui.api.widget;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Unmodifiable;

import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * "Parent" widget that can contain another widgets as children.
 */
public interface IWidgetParent {

    Size getSize();

    Pos2d getAbsolutePos();

    Pos2d getPos();

    @Unmodifiable
    List<Widget> getChildren();

    ModularUIContext getContext();

    /**
     * Called right after the ui is created and right before synced widgets are registered. Last chance to add sub
     * widgets
     */
    default void initChildren() {}

    /**
     * Called during rebuild. {@link Widget#isAutoPositioned()} must be checked for each child!!!
     */
    default void layoutChildren(int maxWidth, int maxHeight) {}

    @SideOnly(Side.CLIENT)
    default void drawChildren(float partialTicks) {
        for (Widget child : getChildren()) {
            child.drawInternal(partialTicks);
        }
    }

    default boolean childrenMustBeInBounds() {
        return false;
    }

    static boolean forEachByLayer(List<Widget> parent, Function<Widget, Boolean> consumer) {
        return forEachByLayer(new Wrapper(parent), consumer);
    }

    static boolean forEachByLayer(Widget parent, Function<Widget, Boolean> consumer) {
        if (parent instanceof IWidgetParent) {
            return forEachByLayer((IWidgetParent) parent, consumer);
        }
        return consumer.apply(parent);
    }

    static boolean forEachByLayer(IWidgetParent parent, Function<Widget, Boolean> consumer) {
        return forEachByLayer(parent, false, consumer);
    }

    static boolean forEachByLayer(IWidgetParent parent, boolean onlyEnabled, Function<Widget, Boolean> consumer) {
        return forEachByLayer(parent, onlyEnabled, widget -> false, consumer);
    }

    static boolean forEachByLayer(IWidgetParent parent, boolean onlyEnabled,
            Function<Widget, Boolean> skipChildrenSearch, Function<Widget, Boolean> consumer) {
        LinkedList<IWidgetParent> stack = new LinkedList<>();
        stack.addLast(parent);
        while (!stack.isEmpty()) {
            IWidgetParent parent1 = stack.pollFirst();
            for (Widget child : parent1.getChildren()) {
                if (onlyEnabled && !child.isEnabled()) {
                    continue;
                }
                if (consumer.apply(child)) {
                    return false;
                }
                if (skipChildrenSearch.apply(child)) {
                    continue;
                }
                if (child instanceof IWidgetParent) {
                    stack.addLast((IWidgetParent) child);
                }
            }
        }
        return true;
    }

    static boolean forEachByBranch(IWidgetParent parent, Function<Widget, Boolean> consumer) {
        for (Widget widget : parent.getChildren()) {
            if (consumer.apply(widget)) {
                return false;
            }
            if (widget instanceof IWidgetParent) {
                forEachByBranch((IWidgetParent) widget, consumer);
            }
        }
        return true;
    }

    static boolean forEachByLayer(Widget parent, Consumer<Widget> consumer) {
        return forEachByLayer(parent, widget -> {
            consumer.accept(widget);
            return false;
        });
    }

    static boolean forEachByLayer(IWidgetParent parent, Consumer<Widget> consumer) {
        return forEachByLayer(parent, widget -> {
            consumer.accept(widget);
            return false;
        });
    }

    static Size getSizeOf(List<Widget> widgets) {
        if (widgets.isEmpty()) return new Size(0, 0);

        int xMin = Integer.MAX_VALUE, yMin = Integer.MAX_VALUE;
        for (Widget widget : widgets) {
            xMin = Math.min(xMin, widget.getPos().x);
            yMin = Math.min(yMin, widget.getPos().y);
        }

        int xMax = Integer.MIN_VALUE, yMax = Integer.MIN_VALUE;
        for (Widget widget : widgets) {
            xMax = Math.max(xMax, widget.getPos().x + widget.getSize().width);
            yMax = Math.max(yMax, widget.getPos().y + widget.getSize().height);
        }
        return new Size(xMax - xMin, yMax - yMin);
    }

    class Wrapper implements IWidgetParent {

        private final List<Widget> children;

        public Wrapper(List<Widget> children) {
            this.children = children;
        }

        @Override
        public Size getSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Pos2d getAbsolutePos() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Pos2d getPos() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Widget> getChildren() {
            return children;
        }

        @Override
        public ModularUIContext getContext() {
            throw new UnsupportedOperationException();
        }
    }
}
