package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSubgroupFeatureFlag implements IntEnum {
 	VK_SUBGROUP_FEATURE_BASIC_BIT(1), 	
 	VK_SUBGROUP_FEATURE_VOTE_BIT(2), 	
 	VK_SUBGROUP_FEATURE_ARITHMETIC_BIT(4), 	
 	VK_SUBGROUP_FEATURE_BALLOT_BIT(8), 	
 	VK_SUBGROUP_FEATURE_SHUFFLE_BIT(16), 	
 	VK_SUBGROUP_FEATURE_SHUFFLE_RELATIVE_BIT(32), 	
 	VK_SUBGROUP_FEATURE_CLUSTERED_BIT(64), 	
 	VK_SUBGROUP_FEATURE_QUAD_BIT(128), 	
 	VK_SUBGROUP_FEATURE_PARTITIONED_BIT_NV(256), 	
 	VK_SUBGROUP_FEATURE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkSubgroupFeatureFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
