package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineInputAssemblyStateCreateInfo extends VulkanStructure {
	public final VkStructureType sType = VkStructureType.PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public VkPrimitiveTopology topology;
	public boolean primitiveRestartEnable;
}
