package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
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
	public static class ByValue extends VkDebugMarkerMarkerInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugMarkerMarkerInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_MARKER_MARKER_INFO_EXT;
	public Pointer pNext;
	public String pMarkerName;
	public float[] color = new float[4];
}
