package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.common.Handle;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.platform.vulkan.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSamplerCreateInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public EnumMask<VkSamplerCreateFlags> flags;
	public VkFilter magFilter;
	public VkFilter minFilter;
	public VkSamplerMipmapMode mipmapMode;
	public VkSamplerAddressMode addressModeU;
	public VkSamplerAddressMode addressModeV;
	public VkSamplerAddressMode addressModeW;
	public float mipLodBias;
	public boolean anisotropyEnable;
	public float maxAnisotropy;
	public boolean compareEnable;
	public VkCompareOp compareOp;
	public float minLod;
	public float maxLod;
	public VkBorderColor borderColor;
	public boolean unnormalizedCoordinates;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("flags"),
			JAVA_INT.withName("magFilter"),
			JAVA_INT.withName("minFilter"),
			JAVA_INT.withName("mipmapMode"),
			JAVA_INT.withName("addressModeU"),
			JAVA_INT.withName("addressModeV"),
			JAVA_INT.withName("addressModeW"),
			JAVA_FLOAT.withName("mipLodBias"),
			JAVA_INT.withName("anisotropyEnable"),
			JAVA_FLOAT.withName("maxAnisotropy"),
			JAVA_INT.withName("compareEnable"),
			JAVA_INT.withName("compareOp"),
			JAVA_FLOAT.withName("minLod"),
			JAVA_FLOAT.withName("maxLod"),
			JAVA_INT.withName("borderColor"),
			JAVA_INT.withName("unnormalizedCoordinates")
		);
	}
}
