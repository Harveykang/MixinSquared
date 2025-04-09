package com.bawnorton.mixinsquared.api;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.List;

public interface TargetModifier {
	/**
	 * @return the name of the mixin class that should apply the target modifier to
	 */
	String getMixinClassName();

	/**
	 * @param originalTargets the list of original target class names
	 * @return null or originalTargets to cancel applying the target modifier,
	 *         otherwise a list of modified target class names
	 */
	List<String> getTargets(List<String> originalTargets);

	/**
	 * Once a mixin class has been modified by a target modifier,
	 * it will ignore the result of {@link IMixinConfigPlugin#shouldApplyMixin(String, String)}.
	 * You can use this method to check if the target modifier should apply to the target class.
	 * @see org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin#shouldApplyMixin(String, String)
	 */
	boolean shouldApplyMixin(String targetClassName);
}
