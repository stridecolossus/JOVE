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
	"pMarkerName",
	"color"
})
public class VkDebugMarkerMarkerInfoEXT extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DEBUG_MARKER_MARKER_INFO_EXT;
	public Pointer pNext;
	public String pMarkerName;
	public float[] color = new float[4];
}
