package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkColorSpaceKHR implements IntegerEnumeration {
 	SRGB_NONLINEAR_KHR(0),
 	DISPLAY_P3_NONLINEAR_EXT(1000104001),
 	EXTENDED_SRGB_LINEAR_EXT(1000104002),
 	DCI_P3_LINEAR_EXT(1000104003),
 	DCI_P3_NONLINEAR_EXT(1000104004),
 	BT709_LINEAR_EXT(1000104005),
 	BT709_NONLINEAR_EXT(1000104006),
 	BT2020_LINEAR_EXT(1000104007),
 	HDR10_ST2084_EXT(1000104008),
 	DOLBYVISION_EXT(1000104009),
 	HDR10_HLG_EXT(1000104010),
 	ADOBERGB_LINEAR_EXT(1000104011),
 	ADOBERGB_NONLINEAR_EXT(1000104012),
 	PASS_THROUGH_EXT(1000104013),
 	EXTENDED_SRGB_NONLINEAR_EXT(1000104014),
 	VK_COLORSPACE_SRGB_NONLINEAR_KHR(0);

	private final int value;

	private VkColorSpaceKHR(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
