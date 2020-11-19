package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
public class VkImagePlaneMemoryRequirementsInfo extends VulkanStructure {
	public static class ByValue extends VkImagePlaneMemoryRequirementsInfo implements Structure.ByValue { }
	public static class ByReference extends VkImagePlaneMemoryRequirementsInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_PLANE_MEMORY_REQUIREMENTS_INFO;
	public Pointer pNext;
	public VkImageAspectFlag planeAspect;
}
