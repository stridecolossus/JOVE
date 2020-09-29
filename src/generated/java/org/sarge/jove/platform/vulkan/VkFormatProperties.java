package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
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
	public static class ByValue extends VkFormatProperties implements Structure.ByValue { }
	public static class ByReference extends VkFormatProperties implements Structure.ByReference { }
	
	public VkFormatFeatureFlags linearTilingFeatures;
	public VkFormatFeatureFlags optimalTilingFeatures;
	public VkFormatFeatureFlags bufferFeatures;
}
