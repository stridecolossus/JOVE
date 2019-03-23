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
	"planeAspect"
})
public class VkBindImagePlaneMemoryInfo extends Structure {
	public static class ByValue extends VkBindImagePlaneMemoryInfo implements Structure.ByValue { }
	public static class ByReference extends VkBindImagePlaneMemoryInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_BIND_IMAGE_PLANE_MEMORY_INFO.value();
	public Pointer pNext;
	public int planeAspect;
}
