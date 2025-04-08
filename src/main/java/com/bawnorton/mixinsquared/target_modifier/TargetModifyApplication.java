package com.bawnorton.mixinsquared.target_modifier;

import com.llamalad7.mixinextras.utils.MixinInternals;
import com.sun.applet2.AppletParameters;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TargetModifyApplication {
	public static final TargetModifyApplication INSTANCE = new TargetModifyApplication();
	private static final Map<String, Set<TargetContext>> modifiers = new ConcurrentHashMap<>();

	public static TargetContext registerModifier(IMixinConfigPlugin plugin, TargetModifier modifier) {
		TargetContext targetContext = new TargetContext(plugin, modifier);
		modifiers.computeIfAbsent(modifier.getMixinClassName(), k -> ConcurrentHashMap.newKeySet()).add(targetContext);
		MixinInternals.getTargets()
		return targetContext;
	}

	private TargetModifyApplication() {
	}
}
