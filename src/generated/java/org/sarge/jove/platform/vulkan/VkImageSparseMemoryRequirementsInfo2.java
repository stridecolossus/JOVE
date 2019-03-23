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
	"image"
})
public class VkImageSparseMemoryRequirementsInfo2 extends VulkanStructure {
	public static class ByValue extends VkImageSparseMemoryRequirementsInfo2 implements Structure.ByValue { }
	public static class ByReference extends VkImageSparseMemoryRequirementsInfo2 implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_SPARSE_MEMORY_REQUIREMENTS_INFO_2;
	public Pointer pNext;
	public Pointer image;
}
