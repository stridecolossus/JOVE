package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
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
	"capabilities"
})
public class VkDisplayPlaneCapabilities2KHR extends VulkanStructure {
	public static class ByValue extends VkDisplayPlaneCapabilities2KHR implements Structure.ByValue { }
	public static class ByReference extends VkDisplayPlaneCapabilities2KHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DISPLAY_PLANE_CAPABILITIES_2_KHR;
	public Pointer pNext;
	public VkDisplayPlaneCapabilitiesKHR capabilities;
}
