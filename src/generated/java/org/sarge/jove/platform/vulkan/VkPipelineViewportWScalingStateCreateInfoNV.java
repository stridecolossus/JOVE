package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.VulkanBoolean;
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
	"viewportWScalingEnable",
	"viewportCount",
	"pViewportWScalings"
})
public class VkPipelineViewportWScalingStateCreateInfoNV extends VulkanStructure {
	public static class ByValue extends VkPipelineViewportWScalingStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineViewportWScalingStateCreateInfoNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_W_SCALING_STATE_CREATE_INFO_NV;
	public Pointer pNext;
	public VulkanBoolean viewportWScalingEnable;
	public int viewportCount;
	public Pointer pViewportWScalings;
}