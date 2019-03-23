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
	"surfaceCapabilities"
})
public class VkSurfaceCapabilities2KHR extends Structure {
	public static class ByValue extends VkSurfaceCapabilities2KHR implements Structure.ByValue { }
	public static class ByReference extends VkSurfaceCapabilities2KHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SURFACE_CAPABILITIES_2_KHR.value();
	public Pointer pNext;
	public VkSurfaceCapabilitiesKHR surfaceCapabilities;
}
