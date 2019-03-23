package org.sarge.jove.platform.vulkan;

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
public class VkPipelineColorBlendAttachmentState extends Structure {
	public static class ByValue extends VkPipelineColorBlendAttachmentState implements Structure.ByValue { }
	public static class ByReference extends VkPipelineColorBlendAttachmentState implements Structure.ByReference { }

	public boolean blendEnable = true;
	public int srcColorBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_SRC_ALPHA.value();
	public int dstColorBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA.value();
	public int colorBlendOp = VkBlendOp.VK_BLEND_OP_ADD.value();
	public int srcAlphaBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_ONE.value();
	public int dstAlphaBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_ZERO.value();
	public int alphaBlendOp = VkBlendOp.VK_BLEND_OP_ADD.value();
	public int colorWriteMask = VkColorComponentFlag.of("RGBA");
}
