package org.sarge.jove.platform.vulkan;

import java.nio.FloatBuffer;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

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
	"framebuffer",
	"renderArea",
	"clearValueCount",
	"pClearValues"
})
public class VkRenderPassBeginInfo extends VulkanStructure {
	public static class ByValue extends VkRenderPassBeginInfo implements Structure.ByValue { }
	public static class ByReference extends VkRenderPassBeginInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
	public Pointer pNext;
	public Pointer renderPass;
	public Pointer framebuffer;
	public VkRect2D renderArea;
	public int clearValueCount;
	public FloatBuffer pClearValues;
}
