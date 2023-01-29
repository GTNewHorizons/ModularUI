package com.gtnewhorizons.modularui.common.widget;

import java.util.ArrayList;
import java.util.List;

import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.Widget;

/**
 * Widget holding a list of {@link TabButton}. Add tab buttons with {@link #addTabButton(Widget)} and add pages with
 * {@link #addPage(Widget)} in corresponding order. At least one child is required. Number of {@link TabButton}s and
 * pages should match.
 */
public class TabContainer extends PageControlWidget {

    private final List<TabButton> tabButtons = new ArrayList<>();
    private final List<Widget> allChildren = new ArrayList<>();
    private Size buttonSize = null;

    @Override
    public void initChildren() {
        allChildren.clear();
        allChildren.addAll(getPages());
        allChildren.addAll(tabButtons);
    }

    @Override
    public void onInit() {
        super.onInit();
        for (TabButton tabButton : tabButtons) {
            tabButton.setTabController(this);
            if (tabButton.getPage() < 0 || tabButton.getPage() >= getPages().size()) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "TabButton page is %s, but must be 0 - %s",
                                tabButton.getPage(),
                                getPages().size() - 1));
            }
            if (buttonSize != null && tabButton.isAutoSized()) {
                tabButton.setSize(buttonSize);
            }
        }
    }

    @Override
    public List<Widget> getChildren() {
        return allChildren;
    }

    /**
     * @param tabButton Must extend {@link TabButton}
     */
    public TabContainer addTabButton(Widget tabButton) {
        if (!(tabButton instanceof TabButton)) {
            throw new IllegalArgumentException("Tab button must be instance of TabButton");
        }
        this.tabButtons.add((TabButton) tabButton);
        return this;
    }

    public TabContainer setButtonSize(Size buttonSize) {
        this.buttonSize = buttonSize;
        return this;
    }

    public TabContainer setButtonSize(int width, int height) {
        return setButtonSize(new Size(width, height));
    }
}
