package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"visibleRegion",
	"refreshRate"
})
public class VkDisplayModeParametersKHR extends Structure {
	public static class ByValue extends VkDisplayModeParametersKHR implements Structure.ByValue { }
	public static class ByReference extends VkDisplayModeParametersKHR implements Structure.ByReference { }
	
	public VkExtent2D visibleRegion;
	public int refreshRate;
}
