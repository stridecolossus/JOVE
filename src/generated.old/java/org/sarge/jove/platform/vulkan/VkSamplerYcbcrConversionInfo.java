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
	"conversion"
})
public class VkSamplerYcbcrConversionInfo extends Structure {
	public static class ByValue extends VkSamplerYcbcrConversionInfo implements Structure.ByValue { }
	public static class ByReference extends VkSamplerYcbcrConversionInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SAMPLER_YCBCR_CONVERSION_INFO.value();
	public Pointer pNext;
	public long conversion;
}
