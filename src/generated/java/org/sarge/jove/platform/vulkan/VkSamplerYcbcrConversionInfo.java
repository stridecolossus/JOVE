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
	"conversion"
})
public class VkSamplerYcbcrConversionInfo extends VulkanStructure {
	public static class ByValue extends VkSamplerYcbcrConversionInfo implements Structure.ByValue { }
	public static class ByReference extends VkSamplerYcbcrConversionInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.SAMPLER_YCBCR_CONVERSION_INFO;
	public Pointer pNext;
	public long conversion;
}
