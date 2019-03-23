package org.sarge.jove.platform.vulkan;

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
public class VkDisplayPropertiesKHR extends Structure {
	public static class ByValue extends VkDisplayPropertiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkDisplayPropertiesKHR implements Structure.ByReference { }
	
	public long display;
	public String displayName;
	public VkExtent2D physicalDimensions;
	public VkExtent2D physicalResolution;
	public int supportedTransforms;
	public boolean planeReorderPossible;
	public boolean persistentContent;
}
