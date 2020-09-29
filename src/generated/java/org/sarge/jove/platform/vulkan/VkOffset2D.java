package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.ScreenCoordinate;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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

	public VkOffset2D() {
	}

	public VkOffset2D(ScreenCoordinate coords) {
		this.x = coords.x;
		this.y = coords.y;
	}

	public VkOffset2D(VkOffset2D offset) {
		x = offset.x;
		y = offset.y;
	}
}
