package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

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
	"filterCubic",
	"filterCubicMinmax"
})
public class VkFilterCubicImageViewImageFormatPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkFilterCubicImageViewImageFormatPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkFilterCubicImageViewImageFormatPropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_FILTER_CUBIC_IMAGE_VIEW_IMAGE_FORMAT_PROPERTIES_EXT;
	public Pointer pNext;
	public VulkanBoolean filterCubic;
	public VulkanBoolean filterCubicMinmax;
}
