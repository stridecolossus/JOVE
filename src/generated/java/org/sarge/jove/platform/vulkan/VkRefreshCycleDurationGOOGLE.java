package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"refreshDuration"
})
public class VkRefreshCycleDurationGOOGLE extends VulkanStructure {
	public static class ByValue extends VkRefreshCycleDurationGOOGLE implements Structure.ByValue { }
	public static class ByReference extends VkRefreshCycleDurationGOOGLE implements Structure.ByReference { }
	
	public long refreshDuration;
}
