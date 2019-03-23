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
	"maxPerSetDescriptors",
	"maxMemoryAllocationSize"
})
public class VkPhysicalDeviceMaintenance3Properties extends Structure {
	public static class ByValue extends VkPhysicalDeviceMaintenance3Properties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMaintenance3Properties implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MAINTENANCE_3_PROPERTIES.value();
	public Pointer pNext;
	public int maxPerSetDescriptors;
	public long maxMemoryAllocationSize;
}
