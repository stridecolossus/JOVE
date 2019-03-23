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
	"flags",
	"displayMode",
	"planeIndex",
	"planeStackIndex",
	"transform",
	"globalAlpha",
	"alphaMode",
	"imageExtent"
})
public class VkDisplaySurfaceCreateInfoKHR extends Structure {
	public static class ByValue extends VkDisplaySurfaceCreateInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkDisplaySurfaceCreateInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DISPLAY_SURFACE_CREATE_INFO_KHR.value();
	public Pointer pNext;
	public int flags;
	public long displayMode;
	public int planeIndex;
	public int planeStackIndex;
	public int transform;
	public float globalAlpha;
	public int alphaMode;
	public VkExtent2D imageExtent;
}
