package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"offset",
	"extent",
	"layer"
})
public class VkRectLayerKHR extends VulkanStructure {
	public static class ByValue extends VkRectLayerKHR implements Structure.ByValue { }
	public static class ByReference extends VkRectLayerKHR implements Structure.ByReference { }
	
	public VkOffset2D offset;
	public VkExtent2D extent;
	public int layer;
}
