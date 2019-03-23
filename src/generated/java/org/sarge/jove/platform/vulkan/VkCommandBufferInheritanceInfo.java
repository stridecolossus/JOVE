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
	"renderPass",
	"subpass",
	"framebuffer",
	"occlusionQueryEnable",
	"queryFlags",
	"pipelineStatistics"
})
public class VkCommandBufferInheritanceInfo extends VulkanStructure {
	public static class ByValue extends VkCommandBufferInheritanceInfo implements Structure.ByValue { }
	public static class ByReference extends VkCommandBufferInheritanceInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO;
	public Pointer pNext;
	public Pointer renderPass;
	public int subpass;
	public Pointer framebuffer;
	public VulkanBoolean occlusionQueryEnable;
	public VkQueryControlFlags queryFlags;
	public VkQueryPipelineStatisticFlags pipelineStatistics;
}
