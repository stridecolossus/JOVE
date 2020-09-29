package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.ByReference;
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
public class VkDescriptorImageInfo extends VulkanStructure implements ByReference {
	public Pointer sampler;
	public Pointer imageView;
	public VkImageLayout imageLayout;
}
