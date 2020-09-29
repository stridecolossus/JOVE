package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkVendorId implements IntegerEnumeration {
 	VK_VENDOR_ID_VIV(65537), 	
 	VK_VENDOR_ID_VSI(65538), 	
 	VK_VENDOR_ID_KAZAN(65539), 	
 	VK_VENDOR_ID_BEGIN_RANGE(65537), 	
 	VK_VENDOR_ID_END_RANGE(65539), 	
 	VK_VENDOR_ID_RANGE_SIZE(3), 	
 	VK_VENDOR_ID_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkVendorId(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
