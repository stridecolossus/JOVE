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
	"objectHandle",
	"tagName",
	"tagSize",
	"pTag"
})
public class VkDebugUtilsObjectTagInfoEXT extends VulkanStructure {
	public static class ByValue extends VkDebugUtilsObjectTagInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugUtilsObjectTagInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_UTILS_OBJECT_TAG_INFO_EXT;
	public Pointer pNext;
	public VkObjectType objectType;
	public long objectHandle;
	public long tagName;
	public long tagSize;
	public Pointer pTag;
}