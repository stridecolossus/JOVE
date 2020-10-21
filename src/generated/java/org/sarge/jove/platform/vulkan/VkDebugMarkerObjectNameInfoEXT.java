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
	"objectType",
	"object",
	"pObjectName"
})
public class VkDebugMarkerObjectNameInfoEXT extends VulkanStructure {
	public static class ByValue extends VkDebugMarkerObjectNameInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugMarkerObjectNameInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_MARKER_OBJECT_NAME_INFO_EXT;
	public Pointer pNext;
	public VkDebugReportObjectTypeEXT objectType;
	public long object;
	public String pObjectName;
}
