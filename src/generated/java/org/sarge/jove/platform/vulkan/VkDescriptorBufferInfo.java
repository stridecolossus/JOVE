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
	"buffer",
	"offset",
	"range"
})
public class VkDescriptorBufferInfo extends VulkanStructure {
	public static class ByValue extends VkDescriptorBufferInfo implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorBufferInfo implements Structure.ByReference { }
	
	public Pointer buffer;
	public long offset;
	public long range;
}
