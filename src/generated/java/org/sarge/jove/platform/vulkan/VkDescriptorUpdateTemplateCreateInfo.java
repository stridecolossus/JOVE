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
public class VkDescriptorUpdateTemplateCreateInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int flags;
	public int descriptorUpdateEntryCount;
	public VkDescriptorUpdateTemplateEntry[] pDescriptorUpdateEntries;
	public VkDescriptorUpdateTemplateType templateType;
	public Handle descriptorSetLayout;
	public VkPipelineBindPoint pipelineBindPoint;
	public Handle pipelineLayout;
	public int set;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("flags"),
			JAVA_INT.withName("descriptorUpdateEntryCount"),
			POINTER.withName("pDescriptorUpdateEntries"),
			JAVA_INT.withName("templateType"),
			PADDING,
			POINTER.withName("descriptorSetLayout"),
			JAVA_INT.withName("pipelineBindPoint"),
			PADDING,
			POINTER.withName("pipelineLayout"),
			JAVA_INT.withName("set")
		);
	}
}
