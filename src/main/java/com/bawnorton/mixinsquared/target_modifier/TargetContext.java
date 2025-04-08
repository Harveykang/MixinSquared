package com.bawnorton.mixinsquared.target_modifier;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.List;

public class TargetContext {
	private final IMixinConfigPlugin plugin;
	private final TargetModifier modifier;

	TargetContext(IMixinConfigPlugin plugin, TargetModifier modifier) {
		this.plugin = plugin;
		this.modifier = modifier;
	}

	public List<String> getModifiedMixins() {

	}
}
