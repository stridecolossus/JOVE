package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"buffer",
	"offset",
	"flags"
})
public class VkConditionalRenderingBeginInfoEXT extends VulkanStructure {
	public VkStructureType sType = VkStructureType.CONDITIONAL_RENDERING_BEGIN_INFO_EXT;
	public Pointer pNext;
	public Pointer buffer;
	public long offset;
	public int flags;
}
