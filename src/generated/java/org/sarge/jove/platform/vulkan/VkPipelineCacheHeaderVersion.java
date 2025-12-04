package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPipelineCacheHeaderVersion implements IntEnum {
	ONE(1),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkPipelineCacheHeaderVersion(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
