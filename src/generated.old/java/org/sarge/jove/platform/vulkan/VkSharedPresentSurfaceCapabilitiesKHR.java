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
	"sharedPresentSupportedUsageFlags"
})
public class VkSharedPresentSurfaceCapabilitiesKHR extends Structure {
	public static class ByValue extends VkSharedPresentSurfaceCapabilitiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkSharedPresentSurfaceCapabilitiesKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SHARED_PRESENT_SURFACE_CAPABILITIES_KHR.value();
	public Pointer pNext;
	public int sharedPresentSupportedUsageFlags;
}
