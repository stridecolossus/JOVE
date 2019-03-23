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
	"pMarkerName",
	"color"
})
public class VkDebugMarkerMarkerInfoEXT extends Structure {
	public static class ByValue extends VkDebugMarkerMarkerInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugMarkerMarkerInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_MARKER_MARKER_INFO_EXT.value();
	public Pointer pNext;
	public String pMarkerName;
	public final float[] color = new float[4];
}
