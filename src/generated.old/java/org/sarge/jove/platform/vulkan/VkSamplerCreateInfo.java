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
	"flags",
	"magFilter",
	"minFilter",
	"mipmapMode",
	"addressModeU",
	"addressModeV",
	"addressModeW",
	"mipLodBias",
	"anisotropyEnable",
	"maxAnisotropy",
	"compareEnable",
	"compareOp",
	"minLod",
	"maxLod",
	"borderColor",
	"unnormalizedCoordinates"
})
public class VkSamplerCreateInfo extends Structure {
	public static class ByValue extends VkSamplerCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkSamplerCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int magFilter;
	public int minFilter;
	public int mipmapMode;
	public int addressModeU;
	public int addressModeV;
	public int addressModeW;
	public float mipLodBias;
	public boolean anisotropyEnable;
	public float maxAnisotropy;
	public boolean compareEnable;
	public int compareOp;
	public float minLod;
	public float maxLod;
	public int borderColor;
	public boolean unnormalizedCoordinates;
}
