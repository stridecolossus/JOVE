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
	public boolean depthTestEnable;
	public boolean depthWriteEnable;
	public VkCompareOp depthCompareOp;
	public boolean depthBoundsTestEnable;
	public boolean stencilTestEnable;
	public VkStencilOpState front;
	public VkStencilOpState back;
	public float minDepthBounds;
	public float maxDepthBounds;
}
