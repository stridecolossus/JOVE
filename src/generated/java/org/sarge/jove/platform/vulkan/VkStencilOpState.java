package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"failOp",
	"passOp",
	"depthFailOp",
	"compareOp",
	"compareMask",
	"writeMask",
	"reference"
})
public class VkStencilOpState extends VulkanStructure {
	public VkStencilOp failOp;
	public VkStencilOp passOp;
	public VkStencilOp depthFailOp;
	public VkCompareOp compareOp;
	public int compareMask;
	public int writeMask;
	public int reference;
}
