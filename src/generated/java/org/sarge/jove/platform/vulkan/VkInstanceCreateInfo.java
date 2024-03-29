package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"pApplicationInfo",
	"enabledLayerCount",
	"ppEnabledLayerNames",
	"enabledExtensionCount",
	"ppEnabledExtensionNames"
})
public class VkInstanceCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.INSTANCE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public VkApplicationInfo pApplicationInfo;
	public int enabledLayerCount;
	public Pointer ppEnabledLayerNames;
	public int enabledExtensionCount;
	public Pointer ppEnabledExtensionNames;
}
