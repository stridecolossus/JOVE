package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDriverIdKHR implements IntegerEnumeration {
 	VK_DRIVER_ID_AMD_PROPRIETARY_KHR(1), 	
 	VK_DRIVER_ID_AMD_OPEN_SOURCE_KHR(2), 	
 	VK_DRIVER_ID_MESA_RADV_KHR(3), 	
 	VK_DRIVER_ID_NVIDIA_PROPRIETARY_KHR(4), 	
 	VK_DRIVER_ID_INTEL_PROPRIETARY_WINDOWS_KHR(5), 	
 	VK_DRIVER_ID_INTEL_OPEN_SOURCE_MESA_KHR(6), 	
 	VK_DRIVER_ID_IMAGINATION_PROPRIETARY_KHR(7), 	
 	VK_DRIVER_ID_QUALCOMM_PROPRIETARY_KHR(8), 	
 	VK_DRIVER_ID_ARM_PROPRIETARY_KHR(9), 	
 	VK_DRIVER_ID_GOOGLE_PASTEL_KHR(10), 	
 	VK_DRIVER_ID_BEGIN_RANGE_KHR(1), 	
 	VK_DRIVER_ID_END_RANGE_KHR(10), 	
 	VK_DRIVER_ID_RANGE_SIZE_KHR(10), 	
 	VK_DRIVER_ID_MAX_ENUM_KHR(2147483647); 	

	private final int value;
	
	private VkDriverIdKHR(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
