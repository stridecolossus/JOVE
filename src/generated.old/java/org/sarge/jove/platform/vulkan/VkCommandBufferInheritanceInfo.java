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
	"renderPass",
	"subpass",
	"framebuffer",
	"occlusionQueryEnable",
	"queryFlags",
	"pipelineStatistics"
})
public class VkCommandBufferInheritanceInfo extends Structure {
	public static class ByValue extends VkCommandBufferInheritanceInfo implements Structure.ByValue { }
	public static class ByReference extends VkCommandBufferInheritanceInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO.value();
	public Pointer pNext;
	public long renderPass;
	public int subpass;
	public long framebuffer;
	public boolean occlusionQueryEnable;
	public int queryFlags;
	public int pipelineStatistics;
}
