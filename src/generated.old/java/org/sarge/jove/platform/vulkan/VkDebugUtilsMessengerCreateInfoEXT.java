package org.sarge.jove.platform.vulkan;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"messageSeverity",
	"messageType",
	"pfnUserCallback",
	"pUserData"
})
public class VkDebugUtilsMessengerCreateInfoEXT extends Structure {
	public static class ByValue extends VkDebugUtilsMessengerCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugUtilsMessengerCreateInfoEXT implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public int flags;
	public int messageSeverity;
	public int messageType;
	public Callback pfnUserCallback;
	public Pointer pUserData;
}
