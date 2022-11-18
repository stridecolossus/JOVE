package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitField;

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
	public BitField<VkMemoryProperty> propertyFlags;
	public int heapIndex;
}
