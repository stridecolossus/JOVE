package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineDynamicStateCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.PIPELINE_DYNAMIC_STATE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public int dynamicStateCount;
	public int[] pDynamicStates;
}
