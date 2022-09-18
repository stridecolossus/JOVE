package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

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
public class VkDisplaySurfaceCreateInfoKHR extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DISPLAY_SURFACE_CREATE_INFO_KHR;
	public Pointer pNext;
	public int flags;
	public long displayMode;
	public int planeIndex;
	public int planeStackIndex;
	public VkSurfaceTransformFlagKHR transform;
	public float globalAlpha;
	public VkDisplayPlaneAlphaFlagKHR alphaMode;
	public VkExtent2D imageExtent;
}
