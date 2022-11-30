package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
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
	"renderPass",
	"framebuffer",
	"renderArea",
	"clearValueCount",
	"pClearValues"
})
public class VkRenderPassBeginInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.RENDER_PASS_BEGIN_INFO;
	public Pointer pNext;
	public Handle renderPass;
	public Handle framebuffer;
	public VkRect2D renderArea;
	public int clearValueCount;
	public VkClearValue.ByReference pClearValues;
}
