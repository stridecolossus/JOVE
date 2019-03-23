package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"combinedImageSamplerDescriptorCount"
})
public class VkSamplerYcbcrConversionImageFormatProperties extends Structure {
	public static class ByValue extends VkSamplerYcbcrConversionImageFormatProperties implements Structure.ByValue { }
	public static class ByReference extends VkSamplerYcbcrConversionImageFormatProperties implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SAMPLER_YCBCR_CONVERSION_IMAGE_FORMAT_PROPERTIES.value();
	public Pointer pNext;
	public int combinedImageSamplerDescriptorCount;
}
