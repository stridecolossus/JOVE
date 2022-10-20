package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	public long display;
	public String displayName;
	public VkExtent2D physicalDimensions;
	public VkExtent2D physicalResolution;
	public VkSurfaceTransformFlagKHR supportedTransforms;
	public boolean planeReorderPossible;
	public boolean persistentContent;
}
