package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkQueryResultFlag implements IntegerEnumeration {
 	LONG(1),
 	WAIT(2),
 	WITH_AVAILABILITY(4),
 	PARTIAL(8);

	private final int value;

	private VkQueryResultFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
