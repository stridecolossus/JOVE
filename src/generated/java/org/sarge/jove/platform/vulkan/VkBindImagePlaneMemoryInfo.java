package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import com.sun.jna.Pointer;

import com.sun.jna.Structure;
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
	public static class ByValue extends VkBindImagePlaneMemoryInfo implements Structure.ByValue { }
	public static class ByReference extends VkBindImagePlaneMemoryInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_BIND_IMAGE_PLANE_MEMORY_INFO;
	public Pointer pNext;
	public VkImageAspectFlagBits planeAspect;
}
