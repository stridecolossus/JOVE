package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineTessellationStateCreateInfo extends VulkanStructure {
	public final VkStructureType sType = VkStructureType.PIPELINE_TESSELLATION_STATE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public int patchControlPoints;
}
