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
	"mode",
	"planeIndex"
})
public class VkDisplayPlaneInfo2KHR extends Structure {
	public static class ByValue extends VkDisplayPlaneInfo2KHR implements Structure.ByValue { }
	public static class ByReference extends VkDisplayPlaneInfo2KHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DISPLAY_PLANE_INFO_2_KHR.value();
	public Pointer pNext;
	public long mode;
	public int planeIndex;
}
