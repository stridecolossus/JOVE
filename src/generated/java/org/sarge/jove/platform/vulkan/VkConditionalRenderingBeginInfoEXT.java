package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	"buffer",
	"offset",
	"flags"
})
public class VkConditionalRenderingBeginInfoEXT extends VulkanStructure {
	public static class ByValue extends VkConditionalRenderingBeginInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkConditionalRenderingBeginInfoEXT implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.CONDITIONAL_RENDERING_BEGIN_INFO_EXT;
	public Pointer pNext;
	public Pointer buffer;
	public long offset;
	public int flags;
}
