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
public class VkPhysicalDeviceInlineUniformBlockPropertiesEXT implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int maxInlineUniformBlockSize;
	public int maxPerStageDescriptorInlineUniformBlocks;
	public int maxPerStageDescriptorUpdateAfterBindInlineUniformBlocks;
	public int maxDescriptorSetInlineUniformBlocks;
	public int maxDescriptorSetUpdateAfterBindInlineUniformBlocks;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("maxInlineUniformBlockSize"),
			JAVA_INT.withName("maxPerStageDescriptorInlineUniformBlocks"),
			JAVA_INT.withName("maxPerStageDescriptorUpdateAfterBindInlineUniformBlocks"),
			JAVA_INT.withName("maxDescriptorSetInlineUniformBlocks"),
			JAVA_INT.withName("maxDescriptorSetUpdateAfterBindInlineUniformBlocks"),
			PADDING
		);
	}
}
