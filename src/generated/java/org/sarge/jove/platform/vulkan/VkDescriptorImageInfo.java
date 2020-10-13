package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sampler",
	"imageView",
	"imageLayout"
})
public class VkDescriptorImageInfo extends VulkanStructure { // implements ByReference {
	public Handle sampler;
	public Handle imageView;
	public VkImageLayout imageLayout;
}
