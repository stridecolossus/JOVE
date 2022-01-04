package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"width",
	"height",
	"depth"
})
public class VkExtent3D extends VulkanStructure {
	public int width;
	public int height;
	public int depth;
}
