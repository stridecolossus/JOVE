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
	"depthClampEnable",
	"rasterizerDiscardEnable",
	"polygonMode",
	"cullMode",
	"frontFace",
	"depthBiasEnable",
	"depthBiasConstantFactor",
	"depthBiasClamp",
	"depthBiasSlopeFactor",
	"lineWidth"
})
public class VkPipelineRasterizationStateCreateInfo extends VulkanStructure {
	public static class ByValue extends VkPipelineRasterizationStateCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineRasterizationStateCreateInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public VulkanBoolean depthClampEnable = VulkanBoolean.FALSE;
	public VulkanBoolean rasterizerDiscardEnable = VulkanBoolean.FALSE;
	public VkPolygonMode polygonMode = VkPolygonMode.VK_POLYGON_MODE_FILL;
	public VkCullModeFlag cullMode = VkCullModeFlag.VK_CULL_MODE_BACK_BIT;
	public VkFrontFace frontFace = VkFrontFace.VK_FRONT_FACE_CLOCKWISE;
	public VulkanBoolean depthBiasEnable = VulkanBoolean.FALSE;
	public float depthBiasConstantFactor;
	public float depthBiasClamp;
	public float depthBiasSlopeFactor;
	public float lineWidth = 1;
}
