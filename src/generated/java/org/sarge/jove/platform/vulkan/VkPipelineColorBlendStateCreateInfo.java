package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
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
public class VkPipelineColorBlendStateCreateInfo extends VulkanStructure {
	public static class ByValue extends VkPipelineColorBlendStateCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineColorBlendStateCreateInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public VulkanBoolean logicOpEnable = VulkanBoolean.FALSE;
	public VkLogicOp logicOp;
	public int attachmentCount;
	public Pointer pAttachments;
	public float[] blendConstants = new float[4];
}
