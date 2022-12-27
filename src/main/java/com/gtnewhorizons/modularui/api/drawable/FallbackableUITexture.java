package com.gtnewhorizons.modularui.api.drawable;

import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

/**
 * Wrapper for {@link UITexture} that you can specify candidate and fallback. If resource path for candidate
 * doesn't exist, fallback is automatically selected.
 */
public class FallbackableUITexture {

    private final UITexture candidate;
    private final Object fallback;
    private Boolean useFallback = null;

    private static final List<FallbackableUITexture> ALL_INSTANCES = new ArrayList<>();

    /**
     * @param candidate Texture to look for first.
     * @param fallback Texture to look for second.
     */
    public FallbackableUITexture(UITexture candidate, UITexture fallback) {
        this(candidate, (Object) fallback);
    }

    /**
     * @param candidate Texture to look for first.
     * @param fallback Fallback object to look for second. You can chain multiple fallbacks.
     */
    public FallbackableUITexture(UITexture candidate, FallbackableUITexture fallback) {
        this(candidate, (Object) fallback);
    }

    /**
     * @param fallback Texture to always fall back to.
     */
    public FallbackableUITexture(UITexture fallback) {
        this(null, fallback);
    }

    private FallbackableUITexture(UITexture candidate, Object fallback) {
        this.candidate = candidate;
        this.fallback = fallback;
        ALL_INSTANCES.add(this);
    }

    public UITexture get() {
        verifyCandidate();
        if (useFallback) {
            return castFallback();
        } else {
            return candidate;
        }
    }

    private void verifyCandidate() {
        if (useFallback == null) {
            if (NetworkUtils.isDedicatedClient()) {
                if (candidate == null) {
                    useFallback = true;
                } else {
                    try {
                        Minecraft.getMinecraft().getResourceManager().getResource(candidate.location);
                        useFallback = false;
                    } catch (IOException e) {
                        useFallback = true;
                    }
                }
            } else {
                useFallback = true;
            }
        }
    }

    private UITexture castFallback() {
        if (fallback instanceof UITexture) {
            return (UITexture) fallback;
        } else if (fallback instanceof FallbackableUITexture) {
            return ((FallbackableUITexture) fallback).get();
        } else {
            throw new RuntimeException("Unexpected type found for fallback: " + fallback.getClass());
        }
    }

    public static void reload() {
        for (FallbackableUITexture t : ALL_INSTANCES) {
            t.useFallback = null;
        }
    }
}
