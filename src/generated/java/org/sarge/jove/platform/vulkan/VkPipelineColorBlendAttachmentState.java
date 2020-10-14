package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"blendEnable",
	"srcColorBlendFactor",
	"dstColorBlendFactor",
	"colorBlendOp",
	"srcAlphaBlendFactor",
	"dstAlphaBlendFactor",
	"alphaBlendOp",
	"colorWriteMask"
})
public class VkPipelineColorBlendAttachmentState extends VulkanStructure {
	public VulkanBoolean blendEnable;
	public VkBlendFactor srcColorBlendFactor;
	public VkBlendFactor dstColorBlendFactor;
	public VkBlendOp colorBlendOp;
	public VkBlendFactor srcAlphaBlendFactor;
	public VkBlendFactor dstAlphaBlendFactor;
	public VkBlendOp alphaBlendOp;
	public int colorWriteMask;
}
