package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"displayProperties"
})
public class VkDisplayProperties2KHR extends Structure {
	public static class ByValue extends VkDisplayProperties2KHR implements Structure.ByValue { }
	public static class ByReference extends VkDisplayProperties2KHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DISPLAY_PROPERTIES_2_KHR.value();
	public Pointer pNext;
	public VkDisplayPropertiesKHR displayProperties;
}
