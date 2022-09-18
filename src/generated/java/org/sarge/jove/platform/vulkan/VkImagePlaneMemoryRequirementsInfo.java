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
	"planeAspect"
})
public class VkImagePlaneMemoryRequirementsInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.IMAGE_PLANE_MEMORY_REQUIREMENTS_INFO;
	public Pointer pNext;
	public VkImageAspect planeAspect;
}
