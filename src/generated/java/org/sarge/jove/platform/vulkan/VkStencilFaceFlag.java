package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkStencilFaceFlag implements IntegerEnumeration {
 	VK_STENCIL_FACE_FRONT_BIT(1), 	
 	VK_STENCIL_FACE_BACK_BIT(2), 	
 	VK_STENCIL_FRONT_AND_BACK(3), 	
 	VK_STENCIL_FACE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkStencilFaceFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
