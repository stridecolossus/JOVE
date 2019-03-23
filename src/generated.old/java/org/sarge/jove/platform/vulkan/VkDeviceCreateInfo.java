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
	"queueCreateInfoCount",
	"pQueueCreateInfos",
	"enabledLayerCount",
	"ppEnabledLayerNames",
	"enabledExtensionCount",
	"ppEnabledExtensionNames",
	"pEnabledFeatures"
})
public class VkDeviceCreateInfo extends Structure {
	public static class ByValue extends VkDeviceCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkDeviceCreateInfo implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int queueCreateInfoCount;
	public Pointer pQueueCreateInfos;
	public int enabledLayerCount;
	public Pointer ppEnabledLayerNames;
	public int enabledExtensionCount;
	public Pointer ppEnabledExtensionNames;
	public VkPhysicalDeviceFeatures pEnabledFeatures;
}
