package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"r",
	"g",
	"b",
	"a"
})
public class VkComponentMapping extends VulkanStructure {
	public VkComponentSwizzle r;
	public VkComponentSwizzle g;
	public VkComponentSwizzle b;
	public VkComponentSwizzle a;
}
