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
	"objectType",
	"objectHandle",
	"tagName",
	"tagSize",
	"pTag"
})
public class VkDebugUtilsObjectTagInfoEXT extends Structure {
	public static class ByValue extends VkDebugUtilsObjectTagInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugUtilsObjectTagInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_UTILS_OBJECT_TAG_INFO_EXT.value();
	public Pointer pNext;
	public int objectType;
	public long objectHandle;
	public long tagName;
	public long tagSize;
	public Pointer pTag;
}
