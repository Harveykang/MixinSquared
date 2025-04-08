package com.bawnorton.mixinsquared.target_modifier;

import java.util.List;
import java.util.stream.Collectors;

public class Targets {
	private final List<String> modifiedTargets;

	Targets(List<String> originalTargets) {
		this.modifiedTargets = originalTargets.stream()
			.map(target -> {
				target = target.substring(target.lastIndexOf(".") + 1);
				return "MixinSquared$TargetModifier$" + target;
			})
			.collect(Collectors.toList());
	}

	public List<String> getModifiedTargets() {
		return modifiedTargets;
	}
}
