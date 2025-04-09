package com.bawnorton.mixinsquared.target_modifier;

import com.bawnorton.mixinsquared.api.TargetModifier;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

public class TargetModifierRegistrar {
    public static void register(TargetModifier targetModifier) {
        TargetModifierApplication.MODIFIERS.put(targetModifier.getMixinClassName(), targetModifier);
    }
}
