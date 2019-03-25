package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"propertyFlags",
	"heapIndex"
})
public class VkMemoryType extends VulkanStructure {
	public static class ByValue extends VkMemoryType implements Structure.ByValue { }
	public static class ByReference extends VkMemoryType implements Structure.ByReference { }

	public int propertyFlags;
	public int heapIndex;
}
