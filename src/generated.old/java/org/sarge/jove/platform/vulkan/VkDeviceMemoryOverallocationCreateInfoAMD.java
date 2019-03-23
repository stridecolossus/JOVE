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
	"overallocationBehavior"
})
public class VkDeviceMemoryOverallocationCreateInfoAMD extends Structure {
	public static class ByValue extends VkDeviceMemoryOverallocationCreateInfoAMD implements Structure.ByValue { }
	public static class ByReference extends VkDeviceMemoryOverallocationCreateInfoAMD implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_MEMORY_OVERALLOCATION_CREATE_INFO_AMD.value();
	public Pointer pNext;
	public int overallocationBehavior;
}
