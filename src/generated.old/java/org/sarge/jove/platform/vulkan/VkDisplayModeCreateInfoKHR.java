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
	"flags",
	"parameters"
})
public class VkDisplayModeCreateInfoKHR extends Structure {
	public static class ByValue extends VkDisplayModeCreateInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkDisplayModeCreateInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DISPLAY_MODE_CREATE_INFO_KHR.value();
	public Pointer pNext;
	public int flags;
	public VkDisplayModeParametersKHR parameters;
}
