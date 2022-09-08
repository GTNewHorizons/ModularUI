package com.gtnewhorizons.modularui.common.widget;

import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import javax.annotation.Nullable;

/**
 * Tab-styled button widget. Can be switched between other tab buttons by {@link TabContainer}.
 */
public class TabButton extends Widget implements Interactable {

    private final int page;
    private TabContainer tabController;
    private IDrawable[] activeBackground;

    /**
     * @param page Should be unique within {@link TabContainer}
     */
    public TabButton(int page) {
        this.page = page;
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (page != tabController.getCurrentPage()) {
            tabController.setActivePage(page);
        }
        return ClickResult.ACCEPT;
    }

    protected void setTabController(TabContainer tabController) {
        this.tabController = tabController;
    }

    public int getPage() {
        return page;
    }

    @Nullable
    @Override
    public IDrawable[] getBackground() {
        if (isSelected() && activeBackground != null) {
            return activeBackground;
        }
        return super.getBackground();
    }

    public boolean isSelected() {
        return page == tabController.getCurrentPage();
    }

    public TabButton setBackground(boolean active, IDrawable... drawables) {
        if (active) {
            this.activeBackground = drawables;
        } else {
            setBackground(drawables);
        }
        return this;
    }
}
