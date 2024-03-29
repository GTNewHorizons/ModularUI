package com.gtnewhorizons.modularui.api.widget;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;

public interface IVanillaSlot {

    BaseSlot getMcSlot();

    /**
     * Called when slot has actual ItemStack. If slot is empty, {@link Widget#getTooltip()} is called instead.
     */
    default List<String> getExtraTooltip() {
        return Collections.emptyList();
    }

    default Function<List<String>, List<String>> getOverwriteItemStackTooltip() {
        return list -> list;
    }
}
