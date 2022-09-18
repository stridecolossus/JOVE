package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"extensionName",
	"specVersion"
})
public class VkExtensionProperties extends VulkanStructure {
	public byte[] extensionName = new byte[256];
	public int specVersion;
}
