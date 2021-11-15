package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
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
	public VkStructureType sType = VkStructureType.DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT;
	public Pointer pNext;
	public int flags;
	public int messageSeverity;
	public int messageType;
	public Callback pfnUserCallback;
	public Pointer pUserData;
}
