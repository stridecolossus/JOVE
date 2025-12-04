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
public class VkPipelineShaderStageCreateInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public EnumMask<VkPipelineShaderStageCreateFlags> flags;
	public EnumMask<VkShaderStageFlags> stage;
	public Handle module;
	public String pName;
	public VkSpecializationInfo pSpecializationInfo;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("flags"),
			JAVA_INT.withName("stage"),
			POINTER.withName("module"),
			POINTER.withName("pName"),
			POINTER.withName("pSpecializationInfo")
		);
	}
}
