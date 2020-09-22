package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.MessageHandler.MessageCallback;
import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

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
public class VkDebugUtilsMessengerCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkDebugUtilsMessengerCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugUtilsMessengerCreateInfoEXT implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT;
	public Pointer pNext;
	public int flags;
	public int messageSeverity;
	public int messageType;
	public MessageCallback pfnUserCallback;
	public Pointer pUserData;
}
