package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"aspectMask",
	"mipLevel",
	"baseArrayLayer",
	"layerCount"
})
public class VkImageSubresourceLayers extends VulkanStructure {
	public static class ByValue extends VkImageSubresourceLayers implements Structure.ByValue { }
	public static class ByReference extends VkImageSubresourceLayers implements Structure.ByReference { }

	public int aspectMask;
	public int mipLevel;
	public int baseArrayLayer;
	public int layerCount;
}
