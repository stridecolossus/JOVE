package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

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
public class VkPipelineRasterizationStateCreateInfo extends VulkanStructure implements ByReference {
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public VulkanBoolean depthClampEnable;
	public VulkanBoolean rasterizerDiscardEnable;
	public VkPolygonMode polygonMode;
	public VkCullModeFlag cullMode;
	public VkFrontFace frontFace;
	public VulkanBoolean depthBiasEnable;
	public float depthBiasConstantFactor;
	public float depthBiasClamp;
	public float depthBiasSlopeFactor;
	public float lineWidth;
}
