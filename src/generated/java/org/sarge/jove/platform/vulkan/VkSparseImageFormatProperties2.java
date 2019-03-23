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
	"properties"
})
public class VkSparseImageFormatProperties2 extends VulkanStructure {
	public static class ByValue extends VkSparseImageFormatProperties2 implements Structure.ByValue { }
	public static class ByReference extends VkSparseImageFormatProperties2 implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_SPARSE_IMAGE_FORMAT_PROPERTIES_2;
	public Pointer pNext;
	public VkSparseImageFormatProperties properties;
}
