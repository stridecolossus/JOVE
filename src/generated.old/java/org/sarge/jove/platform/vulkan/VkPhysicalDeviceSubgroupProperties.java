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
	"subgroupSize",
	"supportedStages",
	"supportedOperations",
	"quadOperationsInAllStages"
})
public class VkPhysicalDeviceSubgroupProperties extends Structure {
	public static class ByValue extends VkPhysicalDeviceSubgroupProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceSubgroupProperties implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SUBGROUP_PROPERTIES.value();
	public Pointer pNext;
	public int subgroupSize;
	public int supportedStages;
	public int supportedOperations;
	public boolean quadOperationsInAllStages;
}
