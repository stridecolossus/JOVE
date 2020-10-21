package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"heapBudget",
	"heapUsage"
})
public class VkPhysicalDeviceMemoryBudgetPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceMemoryBudgetPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMemoryBudgetPropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MEMORY_BUDGET_PROPERTIES_EXT;
	public Pointer pNext;
	public long[] heapBudget = new long[16];
	public long[] heapUsage = new long[16];
}
