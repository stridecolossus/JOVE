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
	"heapBudget",
	"heapUsage"
})
public class VkPhysicalDeviceMemoryBudgetPropertiesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceMemoryBudgetPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMemoryBudgetPropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MEMORY_BUDGET_PROPERTIES_EXT.value();
	public Pointer pNext;
	public final long[] heapBudget = new long[16];
	public final long[] heapUsage = new long[16];
}
