package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineVertexInputStateCreateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public int vertexBindingDescriptionCount;
	public VkVertexInputBindingDescription[] pVertexBindingDescriptions;
	public int vertexAttributeDescriptionCount;
	public VkVertexInputAttributeDescription[] pVertexAttributeDescriptions;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				JAVA_INT.withName("flags"),
				JAVA_INT.withName("vertexBindingDescriptionCount"),
				POINTER.withName("pVertexBindingDescriptions"),
				JAVA_INT.withName("vertexAttributeDescriptionCount"),
				PADDING,
				POINTER.withName("pVertexAttributeDescriptions")
		);
	}
}
