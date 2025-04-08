package com.bawnorton.mixinsquared.target_modifier;

import java.util.List;

public interface TargetModifier {
	String getMixinClassName();

	List<String> getModifyTargets(List<String> originalTargets);
}
