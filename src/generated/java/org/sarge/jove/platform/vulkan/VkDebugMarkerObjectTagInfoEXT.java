package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
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
	"objectType",
	"object",
	"tagName",
	"tagSize",
	"pTag"
})
public class VkDebugMarkerObjectTagInfoEXT extends VulkanStructure {
	public static class ByValue extends VkDebugMarkerObjectTagInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugMarkerObjectTagInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_MARKER_OBJECT_TAG_INFO_EXT;
	public Pointer pNext;
	public VkDebugReportObjectTypeEXT objectType;
	public long object;
	public long tagName;
	public long tagSize;
	public Pointer pTag;
}
