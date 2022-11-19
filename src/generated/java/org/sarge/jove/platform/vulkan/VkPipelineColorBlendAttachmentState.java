package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Structure.*;

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
public class VkPipelineColorBlendAttachmentState extends VulkanStructure implements ByReference {
	public boolean blendEnable;
	public VkBlendFactor srcColorBlendFactor;
	public VkBlendFactor dstColorBlendFactor;
	public VkBlendOp colorBlendOp;
	public VkBlendFactor srcAlphaBlendFactor;
	public VkBlendFactor dstAlphaBlendFactor;
	public VkBlendOp alphaBlendOp;
	public BitMask<VkColorComponent> colorWriteMask;
}
