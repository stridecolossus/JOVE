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
	"surfaceFormat"
})
public class VkSurfaceFormat2KHR extends Structure {
	public static class ByValue extends VkSurfaceFormat2KHR implements Structure.ByValue { }
	public static class ByReference extends VkSurfaceFormat2KHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SURFACE_FORMAT_2_KHR.value();
	public Pointer pNext;
	public VkSurfaceFormatKHR surfaceFormat;
}
