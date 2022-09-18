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
	"capabilities"
})
public class VkDisplayPlaneCapabilities2KHR extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DISPLAY_PLANE_CAPABILITIES_2_KHR;
	public Pointer pNext;
	public VkDisplayPlaneCapabilitiesKHR capabilities;
}
