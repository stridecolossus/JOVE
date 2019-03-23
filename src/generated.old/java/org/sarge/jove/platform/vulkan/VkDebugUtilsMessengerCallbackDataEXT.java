package org.sarge.jove.platform.vulkan;

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
public class VkDebugUtilsMessengerCallbackDataEXT extends Structure {
	public static class ByValue extends VkDebugUtilsMessengerCallbackDataEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugUtilsMessengerCallbackDataEXT implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CALLBACK_DATA_EXT.value();
	public Pointer pNext;
	public int flags;
	public String pMessageIdName;
	public int messageIdNumber;
	public String pMessage;
	public int queueLabelCount;
	public /*VkDebugUtilsLabelEXT.ByReference*/ Pointer pQueueLabels;
	public int cmdBufLabelCount;
	public /*VkDebugUtilsLabelEXT.ByReference */ Pointer pCmdBufLabels;
	public int objectCount;
	public /*VkDebugUtilsObjectNameInfoEXT.ByReference*/ Pointer pObjects;
}
