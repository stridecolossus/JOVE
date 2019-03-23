package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"binding",
	"stride",
	"inputRate"
})
public class VkVertexInputBindingDescription extends VulkanStructure {
	public static class ByValue extends VkVertexInputBindingDescription implements Structure.ByValue { }
	public static class ByReference extends VkVertexInputBindingDescription implements Structure.ByReference { }
	
	public int binding;
	public int stride;
	public VkVertexInputRate inputRate;
}
