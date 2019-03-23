package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"refreshDuration"
})
public class VkRefreshCycleDurationGOOGLE extends Structure {
	public static class ByValue extends VkRefreshCycleDurationGOOGLE implements Structure.ByValue { }
	public static class ByReference extends VkRefreshCycleDurationGOOGLE implements Structure.ByReference { }
	
	public long refreshDuration;
}
