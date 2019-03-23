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
	"memoryProperties"
})
public class VkPhysicalDeviceMemoryProperties2 extends Structure {
	public static class ByValue extends VkPhysicalDeviceMemoryProperties2 implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMemoryProperties2 implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MEMORY_PROPERTIES_2.value();
	public Pointer pNext;
	public VkPhysicalDeviceMemoryProperties memoryProperties;
}
