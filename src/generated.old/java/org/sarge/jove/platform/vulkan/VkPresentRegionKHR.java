package org.sarge.jove.platform.vulkan;

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
public class VkPresentRegionKHR extends Structure {
	public static class ByValue extends VkPresentRegionKHR implements Structure.ByValue { }
	public static class ByReference extends VkPresentRegionKHR implements Structure.ByReference { }
	
	public int rectangleCount;
	public VkRectLayerKHR.ByReference pRectangles;
}
