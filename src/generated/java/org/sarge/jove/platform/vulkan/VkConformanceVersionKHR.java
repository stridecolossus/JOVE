package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"major",
	"minor",
	"subminor",
	"patch"
})
public class VkConformanceVersionKHR extends VulkanStructure {
	public static class ByValue extends VkConformanceVersionKHR implements Structure.ByValue { }
	public static class ByReference extends VkConformanceVersionKHR implements Structure.ByReference { }
	
	public byte major;
	public byte minor;
	public byte subminor;
	public byte patch;
}
