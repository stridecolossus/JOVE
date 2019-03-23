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
	"pObjectName"
})
public class VkDebugMarkerObjectNameInfoEXT extends Structure {
	public static class ByValue extends VkDebugMarkerObjectNameInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugMarkerObjectNameInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_MARKER_OBJECT_NAME_INFO_EXT.value();
	public Pointer pNext;
	public int objectType;
	public long object;
	public String pObjectName;
}
