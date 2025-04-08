package com.bawnorton.mixinsquared.target_modifier;

import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinConfig$IListenerImpl implements MixinConfig$IListenerDelegate{
	@Override
	public void onPrepare(IMixinInfo mixin) {
		System.out.println("Mixin " + mixin.getClassName() + " is preparing");
	}

	@Override
	public void onInit(IMixinInfo mixin) {
		System.out.println("Mixin " + mixin.getClassName() + " is initializing");
	}
}
