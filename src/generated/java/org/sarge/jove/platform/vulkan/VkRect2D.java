package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

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

	/**
	 * Default constructor.
	 */
	public VkRect2D() {
	}

	/**
	 * Constructor given a rectangle.
	 * @param rect Rectangle
	 */
	public VkRect2D(Rectangle rect) {
		offset = new VkOffset2D(rect.position());
		extent = new VkExtent2D(rect.dimensions());
	}

	/**
	 * Copy constructor.
	 */
	public VkRect2D(VkRect2D rect) {
		offset = new VkOffset2D(rect.offset);
		extent = new VkExtent2D(rect.extent);
	}
}
