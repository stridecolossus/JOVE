package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
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
	public static class ByValue extends VkComponentMapping implements Structure.ByValue { }
	public static class ByReference extends VkComponentMapping implements Structure.ByReference { }
	
	public VkComponentSwizzle r;
	public VkComponentSwizzle g;
	public VkComponentSwizzle b;
	public VkComponentSwizzle a;
}
