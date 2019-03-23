package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.VulkanBoolean;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"display",
	"displayName",
	"physicalDimensions",
	"physicalResolution",
	"supportedTransforms",
	"planeReorderPossible",
	"persistentContent"
})
public class VkDisplayPropertiesKHR extends VulkanStructure {
	public static class ByValue extends VkDisplayPropertiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkDisplayPropertiesKHR implements Structure.ByReference { }
	
	public long display;
	public String displayName;
	public VkExtent2D physicalDimensions;
	public VkExtent2D physicalResolution;
	public VkSurfaceTransformFlagsKHR supportedTransforms;
	public VulkanBoolean planeReorderPossible;
	public VulkanBoolean persistentContent;
}
