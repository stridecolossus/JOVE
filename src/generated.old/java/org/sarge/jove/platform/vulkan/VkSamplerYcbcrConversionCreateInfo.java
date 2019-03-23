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
	"format",
	"ycbcrModel",
	"ycbcrRange",
	"components",
	"xChromaOffset",
	"yChromaOffset",
	"chromaFilter",
	"forceExplicitReconstruction"
})
public class VkSamplerYcbcrConversionCreateInfo extends Structure {
	public static class ByValue extends VkSamplerYcbcrConversionCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkSamplerYcbcrConversionCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SAMPLER_YCBCR_CONVERSION_CREATE_INFO.value();
	public Pointer pNext;
	public int format;
	public int ycbcrModel;
	public int ycbcrRange;
	public VkComponentMapping components;
	public int xChromaOffset;
	public int yChromaOffset;
	public int chromaFilter;
	public boolean forceExplicitReconstruction;
}
