package com.gtnewhorizons.modularui.common.widget;

public class TranslatedVanillaButtonWidget extends VanillaButtonWidget {

    private final String internalName;

    public TranslatedVanillaButtonWidget() {
        internalName = "";
    }

    public TranslatedVanillaButtonWidget(String internalName) {
        this.internalName = internalName;
    }

    public String getInternalName() {
        return internalName;
    }
}
