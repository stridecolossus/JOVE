package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"pObjectName"
})
public class VkDebugUtilsObjectNameInfoEXT extends VulkanStructure {
	public static class ByValue extends VkDebugUtilsObjectNameInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugUtilsObjectNameInfoEXT implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.DEBUG_UTILS_OBJECT_NAME_INFO_EXT;
	public Pointer pNext;
	public VkObjectType objectType;
	public long objectHandle;
	public String pObjectName;
}
