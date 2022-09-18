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
public class VkBindImagePlaneMemoryInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.BIND_IMAGE_PLANE_MEMORY_INFO;
	public Pointer pNext;
	public VkImageAspect planeAspect;
}
