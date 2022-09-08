package com.gtnewhorizons.modularui.api.widget;

import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;

import java.util.Collections;
import java.util.List;

public interface IVanillaSlot {

    BaseSlot getMcSlot();

    default List<String> getExtraTooltip() {
        return Collections.emptyList();
    }
}
