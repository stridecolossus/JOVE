package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.*;

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
	public VkStructureType sType = VkStructureType.PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public boolean depthClampEnable;
	public boolean rasterizerDiscardEnable;
	public VkPolygonMode polygonMode;
	public VkCullMode cullMode;
	public VkFrontFace frontFace;
	public boolean depthBiasEnable;
	public float depthBiasConstantFactor;
	public float depthBiasClamp;
	public float depthBiasSlopeFactor;
	public float lineWidth;
}
