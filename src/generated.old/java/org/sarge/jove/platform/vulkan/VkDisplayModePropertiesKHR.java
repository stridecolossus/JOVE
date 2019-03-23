package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"displayMode",
	"parameters"
})
public class VkDisplayModePropertiesKHR extends Structure {
	public static class ByValue extends VkDisplayModePropertiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkDisplayModePropertiesKHR implements Structure.ByReference { }
	
	public long displayMode;
	public VkDisplayModeParametersKHR parameters;
}
