package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"displayMode",
	"parameters"
})
public class VkDisplayModePropertiesKHR extends VulkanStructure {
	public long displayMode;
	public VkDisplayModeParametersKHR parameters;
}
