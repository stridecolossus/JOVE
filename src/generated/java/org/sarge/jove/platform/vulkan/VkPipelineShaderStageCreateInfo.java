package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineShaderStageCreateInfo extends VulkanStructure {
	public final VkStructureType sType = VkStructureType.PIPELINE_SHADER_STAGE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public VkShaderStage stage;
	public Handle module;
	public String pName;
	public VkSpecializationInfo pSpecializationInfo;
}
