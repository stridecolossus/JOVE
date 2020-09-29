package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"currentDisplay",
	"currentStackIndex"
})
public class VkDisplayPlanePropertiesKHR extends VulkanStructure {
	public static class ByValue extends VkDisplayPlanePropertiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkDisplayPlanePropertiesKHR implements Structure.ByReference { }
	
	public long currentDisplay;
	public int currentStackIndex;
}
