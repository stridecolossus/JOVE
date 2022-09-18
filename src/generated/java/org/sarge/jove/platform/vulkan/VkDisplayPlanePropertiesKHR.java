package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"currentDisplay",
	"currentStackIndex"
})
public class VkDisplayPlanePropertiesKHR extends VulkanStructure {
	public long currentDisplay;
	public int currentStackIndex;
}
