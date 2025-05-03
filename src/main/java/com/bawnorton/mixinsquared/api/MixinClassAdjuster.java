package com.bawnorton.mixinsquared.api;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.List;

public interface MixinClassAdjuster {
    /**
     * @return the name of the mixin class that should apply the class adjuster to
     */
    String getMixinClassName();

    /**
     * @param originalTargets the list of original target class names
     * @return null or originalTargets to cancel applying the class adjuster,
     *         otherwise a list of modified target class names
     * @apiNote Will not be obfuscated, runtime names will be used
     */
    List<String> getTargets(List<String> originalTargets);

    /**
     * @return the new priority of the mixin class, lower priority will be applied first
     * null to keep the original priority
     * @see Mixin#priority()
     */
    Integer getPriority();

    /**
     * @return the name of the refmap config file to use for this class adjuster,
     * or null to use the default refmap
     * @see IMixinConfigPlugin#getRefMapperConfig()
     */
    @Nullable String getRefMapperConfig();

    /**
     * Once a mixin class has been modified by a class adjuster,
     * it will ignore the result of {@link IMixinConfigPlugin#shouldApplyMixin(String, String)}.
     * You can use this method to check if the class adjuster should apply to the target class.
     * @see IMixinConfigPlugin#shouldApplyMixin(String, String)
     */
    default boolean shouldApplyMixin(String targetClassName) {
        return true;
    }
}
