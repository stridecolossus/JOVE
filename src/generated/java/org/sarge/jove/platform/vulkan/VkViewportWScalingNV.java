package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"xcoeff",
	"ycoeff"
})
public class VkViewportWScalingNV extends VulkanStructure {
	public float xcoeff;
	public float ycoeff;
}
