package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"displayProperties"
})
public class VkDisplayProperties2KHR extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DISPLAY_PROPERTIES_2_KHR;
	public Pointer pNext;
	public VkDisplayPropertiesKHR displayProperties;
}
