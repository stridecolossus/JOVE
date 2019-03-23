package org.sarge.jove.platform.vulkan;

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
	"flags"
})
public class VkEventCreateInfo extends VulkanStructure {
	public static class ByValue extends VkEventCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkEventCreateInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_EVENT_CREATE_INFO;
	public Pointer pNext;
	public int flags;
}
