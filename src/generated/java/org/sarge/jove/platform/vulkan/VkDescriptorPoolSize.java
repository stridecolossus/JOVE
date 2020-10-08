package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"type",
	"descriptorCount"
})
public class VkDescriptorPoolSize extends VulkanStructure {
	public VkDescriptorType type;
	public int descriptorCount;
}
