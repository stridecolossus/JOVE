package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"queueCreateInfoCount",
	"pQueueCreateInfos",
	"enabledLayerCount",
	"ppEnabledLayerNames",
	"enabledExtensionCount",
	"ppEnabledExtensionNames",
	"pEnabledFeatures"
})
public class VkDeviceCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public int queueCreateInfoCount;
	public VkDeviceQueueCreateInfo pQueueCreateInfos;
	public int enabledLayerCount;
	public Pointer ppEnabledLayerNames;
	public int enabledExtensionCount;
	public Pointer ppEnabledExtensionNames;
	public VkPhysicalDeviceFeatures pEnabledFeatures;
}
