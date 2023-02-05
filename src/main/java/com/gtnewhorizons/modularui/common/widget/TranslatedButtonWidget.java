package com.gtnewhorizons.modularui.common.widget;

public class TranslatedButtonWidget extends ButtonWidget {

    private final String internalName;

    public TranslatedButtonWidget() {
        internalName = "";
    }

    public TranslatedButtonWidget(String internalName) {
        this.internalName = internalName;
    }

    public String getInternalName() {
        return internalName;
    }
}
