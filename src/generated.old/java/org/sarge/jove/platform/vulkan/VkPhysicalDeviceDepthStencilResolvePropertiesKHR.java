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
	"supportedDepthResolveModes",
	"supportedStencilResolveModes",
	"independentResolveNone",
	"independentResolve"
})
public class VkPhysicalDeviceDepthStencilResolvePropertiesKHR extends Structure {
	public static class ByValue extends VkPhysicalDeviceDepthStencilResolvePropertiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceDepthStencilResolvePropertiesKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DEPTH_STENCIL_RESOLVE_PROPERTIES_KHR.value();
	public Pointer pNext;
	public int supportedDepthResolveModes;
	public int supportedStencilResolveModes;
	public boolean independentResolveNone;
	public boolean independentResolve;
}
