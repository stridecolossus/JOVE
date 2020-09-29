package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"x",
	"y",
	"z",
	"w"
})
public class VkViewportSwizzleNV extends VulkanStructure {
	public static class ByValue extends VkViewportSwizzleNV implements Structure.ByValue { }
	public static class ByReference extends VkViewportSwizzleNV implements Structure.ByReference { }
	
	public VkViewportCoordinateSwizzleNV x;
	public VkViewportCoordinateSwizzleNV y;
	public VkViewportCoordinateSwizzleNV z;
	public VkViewportCoordinateSwizzleNV w;
}
