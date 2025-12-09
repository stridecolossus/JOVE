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
public class VkDescriptorSetLayoutBinding implements NativeStructure {
	public int binding;
	public VkDescriptorType descriptorType;
	public int descriptorCount;
	public EnumMask<VkShaderStageFlags> stageFlags;
	public Handle[] pImmutableSamplers;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("binding"),
			JAVA_INT.withName("descriptorType"),
			JAVA_INT.withName("descriptorCount"),
			JAVA_INT.withName("stageFlags"),
			POINTER.withName("pImmutableSamplers")
		);
	}
}
