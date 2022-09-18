package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	public byte major;
	public byte minor;
	public byte subminor;
	public byte patch;
}
