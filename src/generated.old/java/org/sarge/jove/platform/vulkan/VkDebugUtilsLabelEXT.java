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
	"pLabelName",
	"color"
})
public class VkDebugUtilsLabelEXT extends Structure {
	public static class ByValue extends VkDebugUtilsLabelEXT implements Structure.ByValue { }
	public static class ByReference extends VkDebugUtilsLabelEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_UTILS_LABEL_EXT.value();
	public Pointer pNext;
	public String pLabelName;
	public final float[] color = new float[4];
}
