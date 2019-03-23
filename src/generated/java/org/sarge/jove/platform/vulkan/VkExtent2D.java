package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"width",
	"height"
})
public class VkExtent2D extends VulkanStructure {
	public static class ByValue extends VkExtent2D implements Structure.ByValue { }
	public static class ByReference extends VkExtent2D implements Structure.ByReference { }

	public int width;
	public int height;

	public VkExtent2D() {
	}

	public VkExtent2D(Dimensions dim) {
		width = dim.width;
		height = dim.height;
	}

	public VkExtent2D(VkExtent2D extent) {
		width = extent.width;
		height = extent.height;
	}
}
