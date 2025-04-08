package com.bawnorton.mixinsquared.target_modifier;

import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public interface MixinConfig$IListenerDelegate {
	/**
	 * Called when a mixin has been successfully prepared
	 *
	 * @param mixin mixin which was prepared
	 */
	void onPrepare(IMixinInfo mixin);

	/**
	 * Called when a mixin has completed post-initialisation
	 *
	 * @param mixin mixin which completed postinit
	 */
	void onInit(IMixinInfo mixin);
}
