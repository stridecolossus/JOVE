package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
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
	public static class ByValue extends VkPipelineColorBlendAttachmentState implements Structure.ByValue { }
	public static class ByReference extends VkPipelineColorBlendAttachmentState implements Structure.ByReference { }

	public VulkanBoolean blendEnable = VulkanBoolean.TRUE;
	public VkBlendFactor srcColorBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_SRC_ALPHA;
	public VkBlendFactor dstColorBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
	public VkBlendOp colorBlendOp = VkBlendOp.VK_BLEND_OP_ADD;
	public VkBlendFactor srcAlphaBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_ONE;
	public VkBlendFactor dstAlphaBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_ZERO;
	public VkBlendOp alphaBlendOp = VkBlendOp.VK_BLEND_OP_ADD;
	public int colorWriteMask = VulkanHelper.DEFAULT_COLOUR_COMPONENT;
}
