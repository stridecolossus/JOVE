package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"supportedDepthResolveModes",
	"supportedStencilResolveModes",
	"independentResolveNone",
	"independentResolve"
})
public class VkPhysicalDeviceDepthStencilResolvePropertiesKHR extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceDepthStencilResolvePropertiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceDepthStencilResolvePropertiesKHR implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_DEPTH_STENCIL_RESOLVE_PROPERTIES_KHR;
	public Pointer pNext;
	public VkResolveModeFlagKHR supportedDepthResolveModes;
	public VkResolveModeFlagKHR supportedStencilResolveModes;
	public boolean independentResolveNone;
	public boolean independentResolve;
}
