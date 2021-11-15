package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	"pMessageIdName",
	"messageIdNumber",
	"pMessage",
	"queueLabelCount",
	"pQueueLabels",
	"cmdBufLabelCount",
	"pCmdBufLabels",
	"objectCount",
	"pObjects"
})
public class VkDebugUtilsMessengerCallbackData extends VulkanStructure {
	public static class ByValue extends VkDebugUtilsMessengerCallbackData implements Structure.ByValue { }
	public static class ByReference extends VkDebugUtilsMessengerCallbackData implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.DEBUG_UTILS_MESSENGER_CALLBACK_DATA_EXT;
	public Pointer pNext;
	public int flags;
	public String pMessageIdName;
	public int messageIdNumber;
	public String pMessage;
	public int queueLabelCount;
	public Pointer pQueueLabels;
	public int cmdBufLabelCount;
	public Pointer pCmdBufLabels;
	public int objectCount;
	public Pointer pObjects;
}
