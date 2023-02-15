package com.gtnewhorizons.modularui.common.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.IWidgetParent;
import com.gtnewhorizons.modularui.api.widget.Widget;

public class MultiChildWidget extends Widget implements IWidgetParent {

    protected final List<Widget> children = new ArrayList<>();

    public MultiChildWidget addChild(Widget widget) {
        if (checkChild(this, widget)) {
            children.add(widget);
            checkNeedsRebuild();
        }
        return this;
    }

    public void removeChild(Widget widget) {
        if (checkEditable(this)) {
            children.remove(widget);
            checkNeedsRebuild();
        }
    }

    public void removeChild(int index) {
        if (checkEditable(this)) {
            children.remove(index);
            checkNeedsRebuild();
        }
    }

    @Override
    public List<Widget> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        if (!getChildren().isEmpty()) {
            return IWidgetParent.getSizeOf(getChildren());
        }
        return new Size(maxWidth, maxHeight);
    }

    @Deprecated
    public static Size getSizeOf(List<Widget> widgets) {
        return IWidgetParent.getSizeOf(widgets);
    }

    public static boolean checkChild(Widget parent, Widget widget) {
        if (widget == null) {
            ModularUI.logger.error("Tried adding null widget to " + parent.getClass().getSimpleName());
            return false;
        }
        if (widget == parent) {
            ModularUI.logger.error("Can't add self!");
            return false;
        }
        return checkEditable(parent);
    }

    public static boolean checkEditable(Widget parent) {
        if (parent.isInitialised() && !parent.getContext().isClientOnly()) {
            throw new IllegalStateException("Can only dynamically add/remove widgets when the ui is client only!");
        }
        return true;
    }
}
