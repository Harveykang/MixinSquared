package com.bawnorton.mixinsquared.target_modifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TargetModifyApplication {
	public static final TargetModifyApplication INSTANCE = new TargetModifyApplication();
	private final List<String> modifiers = new ArrayList<>();

	public Targets registerModifier(TargetModifier modifier) {
		return new Targets(modifier);
	}

	private TargetModifyApplication() {
	}

	public List<String> getModifiedMixins() {
		return Collections.unmodifiableList(modifiers);
	}
}
