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
	"pciDomain",
	"pciBus",
	"pciDevice",
	"pciFunction"
})
public class VkPhysicalDevicePCIBusInfoPropertiesEXT extends Structure {
	public static class ByValue extends VkPhysicalDevicePCIBusInfoPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDevicePCIBusInfoPropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PCI_BUS_INFO_PROPERTIES_EXT.value();
	public Pointer pNext;
	public int pciDomain;
	public int pciBus;
	public int pciDevice;
	public int pciFunction;
}
