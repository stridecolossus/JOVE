package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"format",
	"ycbcrModel",
	"ycbcrRange",
	"components",
	"xChromaOffset",
	"yChromaOffset",
	"chromaFilter",
	"forceExplicitReconstruction"
})
public class VkSamplerYcbcrConversionCreateInfo extends VulkanStructure {
	public static class ByValue extends VkSamplerYcbcrConversionCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkSamplerYcbcrConversionCreateInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.SAMPLER_YCBCR_CONVERSION_CREATE_INFO;
	public Pointer pNext;
	public VkFormat format;
	public VkSamplerYcbcrModelConversion ycbcrModel;
	public VkSamplerYcbcrRange ycbcrRange;
	public VkComponentMapping components;
	public VkChromaLocation xChromaOffset;
	public VkChromaLocation yChromaOffset;
	public VkFilter chromaFilter;
	public boolean forceExplicitReconstruction;
}
