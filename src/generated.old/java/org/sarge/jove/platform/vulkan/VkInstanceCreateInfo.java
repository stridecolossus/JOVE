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
	"pApplicationInfo",
	"enabledLayerCount",
	"ppEnabledLayerNames",
	"enabledExtensionCount",
	"ppEnabledExtensionNames"
})
public class VkInstanceCreateInfo extends Structure {
	public static class ByValue extends VkInstanceCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkInstanceCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public VkApplicationInfo.ByReference pApplicationInfo;
	public int enabledLayerCount;
	public Pointer ppEnabledLayerNames;
	public int enabledExtensionCount;
	public Pointer ppEnabledExtensionNames;
}
