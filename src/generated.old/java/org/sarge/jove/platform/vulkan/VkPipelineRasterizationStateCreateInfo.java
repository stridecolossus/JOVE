package org.sarge.jove.platform.vulkan;

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
public class VkPipelineRasterizationStateCreateInfo extends Structure {
	public static class ByValue extends VkPipelineRasterizationStateCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineRasterizationStateCreateInfo implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public boolean depthClampEnable;
	public boolean rasterizerDiscardEnable = true;
	public int polygonMode = VkPolygonMode.VK_POLYGON_MODE_FILL.value();
	public int cullMode = VkCullModeFlag.VK_CULL_MODE_BACK_BIT.value();
	public int frontFace = VkFrontFace.VK_FRONT_FACE_CLOCKWISE.value();
	public boolean depthBiasEnable;
	public float depthBiasConstantFactor;
	public float depthBiasClamp;
	public float depthBiasSlopeFactor;
	public float lineWidth = 1;

	// TODO - builder
}
