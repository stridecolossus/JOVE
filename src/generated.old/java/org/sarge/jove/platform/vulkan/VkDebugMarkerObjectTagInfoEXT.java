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
	"object",
	"tagName",
	"tagSize",
	"pTag"
})
public class VkDebugMarkerObjectTagInfoEXT extends Structure {
	public static class ByValue extends VkDebugMarkerObjectTagInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugMarkerObjectTagInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_MARKER_OBJECT_TAG_INFO_EXT.value();
	public Pointer pNext;
	public int objectType;
	public long object;
	public long tagName;
	public long tagSize;
	public Pointer pTag;
}
