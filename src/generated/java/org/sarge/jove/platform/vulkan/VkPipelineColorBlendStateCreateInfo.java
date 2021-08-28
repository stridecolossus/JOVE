package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.ByReference;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"logicOpEnable",
	"logicOp",
	"attachmentCount",
	"pAttachments",
	"blendConstants"
})
public class VkPipelineColorBlendStateCreateInfo extends VulkanStructure implements ByReference {
	public VkStructureType sType = VkStructureType.PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public VulkanBoolean logicOpEnable;
	public VkLogicOp logicOp;
	public int attachmentCount;
	public VkPipelineColorBlendAttachmentState pAttachments;
	public float[] blendConstants = new float[4];
}
