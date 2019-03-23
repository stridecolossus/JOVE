package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import com.sun.jna.Pointer;

import com.sun.jna.Structure;
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
public class VkDescriptorImageInfo extends VulkanStructure {
	public static class ByValue extends VkDescriptorImageInfo implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorImageInfo implements Structure.ByReference { }
	
	public Pointer sampler;
	public Pointer imageView;
	public VkImageLayout imageLayout;
}
