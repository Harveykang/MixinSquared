package com.bawnorton.mixinsquared.class_adjuster;

import com.bawnorton.mixinsquared.api.MixinClassAdjuster;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.service.MixinService;

public class MixinClassAdjusterRegistrar {
    private static final ILogger LOGGER = MixinService.getService().getLogger("mixinsquared");

    public static void register(MixinClassAdjuster mixinClassAdjuster) {
        MixinClassAdjusterApplication.ADJUSTERS.put(mixinClassAdjuster.getMixinClassName(), mixinClassAdjuster);
        LOGGER.debug("Registered class adjuster {}", mixinClassAdjuster.getClass().getName());
    }
}
