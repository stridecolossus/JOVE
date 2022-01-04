package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"x",
	"y"
})
public class VkOffset2D extends VulkanStructure {
	public static class ByValue extends VkOffset2D implements Structure.ByValue { }
	public static class ByReference extends VkOffset2D implements Structure.ByReference { }

	public int x;
	public int y;
}
