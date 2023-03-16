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
	"renderPass",
	"subpass",
	"framebuffer",
	"occlusionQueryEnable",
	"queryFlags",
	"pipelineStatistics"
})
public class VkCommandBufferInheritanceInfo extends VulkanStructure implements ByReference {
	public VkStructureType sType = VkStructureType.COMMAND_BUFFER_INHERITANCE_INFO;
	public Pointer pNext;
	public Pointer renderPass;
	public int subpass;
	public Pointer framebuffer;
	public boolean occlusionQueryEnable;
	public VkQueryControlFlag queryFlags;
	public VkQueryPipelineStatisticFlag pipelineStatistics;
}
