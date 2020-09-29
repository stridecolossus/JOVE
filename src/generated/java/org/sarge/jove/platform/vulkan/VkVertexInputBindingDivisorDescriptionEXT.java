package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"binding",
	"divisor"
})
public class VkVertexInputBindingDivisorDescriptionEXT extends VulkanStructure {
	public static class ByValue extends VkVertexInputBindingDivisorDescriptionEXT implements Structure.ByValue { }
	public static class ByReference extends VkVertexInputBindingDivisorDescriptionEXT implements Structure.ByReference { }
	
	public int binding;
	public int divisor;
}
