package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

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
public class VkPipelineDepthStencilStateCreateInfo extends Structure {
	public static class ByValue extends VkPipelineDepthStencilStateCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineDepthStencilStateCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public boolean depthTestEnable;
	public boolean depthWriteEnable;
	public int depthCompareOp;
	public boolean depthBoundsTestEnable;
	public boolean stencilTestEnable;
	public VkStencilOpState front;
	public VkStencilOpState back;
	public float minDepthBounds;
	public float maxDepthBounds;
}
