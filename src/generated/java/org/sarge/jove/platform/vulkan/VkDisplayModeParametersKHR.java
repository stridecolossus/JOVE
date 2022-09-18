package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"visibleRegion",
	"refreshRate"
})
public class VkDisplayModeParametersKHR extends VulkanStructure {
	public VkExtent2D visibleRegion;
	public int refreshRate;
}
