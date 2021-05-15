package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkLogicOp implements IntegerEnumeration {
 	CLEAR(0),
 	AND(1),
 	AND_REVERSE(2),
 	COPY(3),
 	AND_INVERTED(4),
 	NO_OP(5),
 	XOR(6),
 	OR(7),
 	NOR(8),
 	EQUIVALENT(9),
 	INVERT(10),
 	OR_REVERSE(11),
 	COPY_INVERTED(12),
 	OR_INVERTED(13),
 	NAND(14),
 	SET(15);

	private final int value;

	private VkLogicOp(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
