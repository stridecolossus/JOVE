package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"rectangleCount",
	"pRectangles"
})
public class VkPresentRegionKHR extends VulkanStructure {
	public static class ByValue extends VkPresentRegionKHR implements Structure.ByValue { }
	public static class ByReference extends VkPresentRegionKHR implements Structure.ByReference { }
	
	public int rectangleCount;
	public Pointer pRectangles;
}
