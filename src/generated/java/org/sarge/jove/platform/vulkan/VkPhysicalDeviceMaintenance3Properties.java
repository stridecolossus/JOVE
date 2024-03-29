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
	"maxPerSetDescriptors",
	"maxMemoryAllocationSize"
})
public class VkPhysicalDeviceMaintenance3Properties extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceMaintenance3Properties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMaintenance3Properties implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_MAINTENANCE_3_PROPERTIES;
	public Pointer pNext;
	public int maxPerSetDescriptors;
	public long maxMemoryAllocationSize;
}
