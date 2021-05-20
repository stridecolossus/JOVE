package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"linearTilingFeatures",
	"optimalTilingFeatures",
	"bufferFeatures"
})
public class VkFormatProperties extends VulkanStructure {
	public int linearTilingFeatures;
	public int optimalTilingFeatures;
	public int bufferFeatures;
}
