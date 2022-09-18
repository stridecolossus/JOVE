package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

import com.sun.jna.Pointer;
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
	public VkStructureType sType = VkStructureType.FILTER_CUBIC_IMAGE_VIEW_IMAGE_FORMAT_PROPERTIES_EXT;
	public Pointer pNext;
	public VulkanBoolean filterCubic;
	public VulkanBoolean filterCubicMinmax;
}
