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
	"surfaceCapabilities"
})
public class VkSurfaceCapabilities2KHR extends VulkanStructure {
	public static class ByValue extends VkSurfaceCapabilities2KHR implements Structure.ByValue { }
	public static class ByReference extends VkSurfaceCapabilities2KHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.SURFACE_CAPABILITIES_2_KHR;
	public Pointer pNext;
	public VkSurfaceCapabilitiesKHR surfaceCapabilities;
}
