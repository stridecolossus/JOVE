package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"queueFamilyProperties"
})
public class VkQueueFamilyProperties2 extends VulkanStructure {
	public static class ByValue extends VkQueueFamilyProperties2 implements Structure.ByValue { }
	public static class ByReference extends VkQueueFamilyProperties2 implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.QUEUE_FAMILY_PROPERTIES_2;
	public Pointer pNext;
	public VkQueueFamilyProperties queueFamilyProperties;
}
