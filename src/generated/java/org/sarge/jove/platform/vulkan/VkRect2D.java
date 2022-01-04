package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"offset",
	"extent"
})
public class VkRect2D extends VulkanStructure {
	public static class ByValue extends VkRect2D implements Structure.ByValue { }
	public static class ByReference extends VkRect2D implements Structure.ByReference { }

	public VkOffset2D offset;
	public VkExtent2D extent;
}
