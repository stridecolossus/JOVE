package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

import com.sun.jna.*;
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
	public BitMask<VkDebugUtilsMessageSeverity> messageSeverity;
	public BitMask<VkDebugUtilsMessageType> messageType;
	public Callback pfnUserCallback;
	public Pointer pUserData;
}
