package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
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
	"depthTestEnable",
	"depthWriteEnable",
	"depthCompareOp",
	"depthBoundsTestEnable",
	"stencilTestEnable",
	"front",
	"back",
	"minDepthBounds",
	"maxDepthBounds"
})
public class VkPipelineDepthStencilStateCreateInfo extends VulkanStructure implements ByReference {
	public VkStructureType sType = VkStructureType.PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public VulkanBoolean depthTestEnable;
	public VulkanBoolean depthWriteEnable;
	public VkCompareOp depthCompareOp;
	public VulkanBoolean depthBoundsTestEnable;
	public VulkanBoolean stencilTestEnable;
	public VkStencilOpState front;
	public VkStencilOpState back;
	public float minDepthBounds;
	public float maxDepthBounds;
}
