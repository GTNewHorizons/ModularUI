package com.gtnewhorizons.modularui.common.widget;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.IWidgetParent;
import com.gtnewhorizons.modularui.api.widget.Widget;

public class SingleChildWidget extends Widget implements IWidgetParent {

    private Widget child;

    public SingleChildWidget() {}

    public SingleChildWidget(Size size) {
        super(size);
    }

    public SingleChildWidget(Size size, Pos2d pos) {
        super(size, pos);
    }

    public final SingleChildWidget setChild(Widget widget) {
        if (this.child != null) {
            ModularUI.logger.error("Child is already set!");
        } else if (MultiChildWidget.checkEditable(this)) {
            this.child = widget;
        }
        return this;
    }

    @Override
    public List<Widget> getChildren() {
        return Collections.singletonList(child);
    }

    public Widget getChild() {
        return child;
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return child.getSize();
    }
}
