package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

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
public class VkDebugUtilsLabelEXT extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DEBUG_UTILS_LABEL_EXT;
	public Pointer pNext;
	public String pLabelName;
	public float[] color = new float[4];
}
