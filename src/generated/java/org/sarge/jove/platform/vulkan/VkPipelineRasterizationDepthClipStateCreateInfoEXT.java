package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

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
	"depthClipEnable"
})
public class VkPipelineRasterizationDepthClipStateCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkPipelineRasterizationDepthClipStateCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineRasterizationDepthClipStateCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PIPELINE_RASTERIZATION_DEPTH_CLIP_STATE_CREATE_INFO_EXT;
	public Pointer pNext;
	public int flags;
	public VulkanBoolean depthClipEnable;
}
