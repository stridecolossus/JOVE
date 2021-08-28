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
	"pciDomain",
	"pciBus",
	"pciDevice",
	"pciFunction"
})
public class VkPhysicalDevicePCIBusInfoPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDevicePCIBusInfoPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDevicePCIBusInfoPropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_PCI_BUS_INFO_PROPERTIES_EXT;
	public Pointer pNext;
	public int pciDomain;
	public int pciBus;
	public int pciDevice;
	public int pciFunction;
}
