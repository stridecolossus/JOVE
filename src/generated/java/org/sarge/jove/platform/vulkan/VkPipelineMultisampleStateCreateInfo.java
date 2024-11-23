package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineMultisampleStateCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public VkSampleCount rasterizationSamples;
	public boolean sampleShadingEnable;
	public float minSampleShading;
	public int[] pSampleMask;
	public boolean alphaToCoverageEnable;
	public boolean alphaToOneEnable;
}
