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
	"filterCubic",
	"filterCubicMinmax"
})
public class VkFilterCubicImageViewImageFormatPropertiesEXT extends Structure {
	public static class ByValue extends VkFilterCubicImageViewImageFormatPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkFilterCubicImageViewImageFormatPropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_FILTER_CUBIC_IMAGE_VIEW_IMAGE_FORMAT_PROPERTIES_EXT.value();
	public Pointer pNext;
	public boolean filterCubic;
	public boolean filterCubicMinmax;
}
