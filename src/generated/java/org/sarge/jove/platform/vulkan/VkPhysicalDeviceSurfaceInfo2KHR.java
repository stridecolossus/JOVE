package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

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
	"surface"
})
public class VkPhysicalDeviceSurfaceInfo2KHR extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceSurfaceInfo2KHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceSurfaceInfo2KHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_SURFACE_INFO_2_KHR;
	public Pointer pNext;
	public long surface;
}
