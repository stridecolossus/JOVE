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
	"surfaceFormat"
})
public class VkSurfaceFormat2KHR extends VulkanStructure {
	public VkStructureType sType = VkStructureType.SURFACE_FORMAT_2_KHR;
	public Pointer pNext;
	public VkSurfaceFormatKHR surfaceFormat;
}
