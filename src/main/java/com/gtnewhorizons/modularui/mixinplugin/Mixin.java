package com.gtnewhorizons.modularui.mixinplugin;

import static com.gtnewhorizons.modularui.mixinplugin.TargetedMod.NOTENOUGHITEMS;
import static com.gtnewhorizons.modularui.mixinplugin.TargetedMod.VANILLA;

import java.util.Arrays;
import java.util.List;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

public enum Mixin {

    //
    // IMPORTANT: Do not make any references to any mod from this file. This file is loaded quite early on and if
    // you refer to other mods you load them as well. The consequence is: You can't inject any previously loaded
    // classes!
    // Exception: Tags.java, as long as it is used for Strings only!
    //

    // Replace with your own mixins:
    GuiContainerAccessor("GuiContainerAccessor", Side.CLIENT, VANILLA),
    NetHandlerPlayClientMixin("NetHandlerPlayClientMixin", Side.CLIENT, VANILLA),
    PacketBufferMixin("PacketBufferMixin", Side.BOTH, VANILLA),
    NEIControllerMixin("NEIControllerMixin", Side.CLIENT, NOTENOUGHITEMS);

    public final String mixinClass;
    public final List<TargetedMod> targetedMods;
    private final Side side;

    Mixin(String mixinClass, Side side, TargetedMod... targetedMods) {
        this.mixinClass = mixinClass;
        this.targetedMods = Arrays.asList(targetedMods);
        this.side = side;
    }

    Mixin(String mixinClass, TargetedMod... targetedMods) {
        this.mixinClass = mixinClass;
        this.targetedMods = Arrays.asList(targetedMods);
        this.side = Side.BOTH;
    }

    public boolean shouldLoad(List<TargetedMod> loadedMods) {
        return (side == Side.BOTH || side == Side.SERVER && FMLLaunchHandler.side().isServer()
                || side == Side.CLIENT && FMLLaunchHandler.side().isClient()) && loadedMods.containsAll(targetedMods);
    }
}

enum Side {
    BOTH,
    CLIENT,
    SERVER;
}
