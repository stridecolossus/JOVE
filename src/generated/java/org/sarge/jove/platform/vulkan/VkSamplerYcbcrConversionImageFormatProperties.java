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
	"combinedImageSamplerDescriptorCount"
})
public class VkSamplerYcbcrConversionImageFormatProperties extends VulkanStructure {
	public static class ByValue extends VkSamplerYcbcrConversionImageFormatProperties implements Structure.ByValue { }
	public static class ByReference extends VkSamplerYcbcrConversionImageFormatProperties implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_SAMPLER_YCBCR_CONVERSION_IMAGE_FORMAT_PROPERTIES;
	public Pointer pNext;
	public int combinedImageSamplerDescriptorCount;
}
