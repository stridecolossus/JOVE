package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"pixelX",
	"pixelY",
	"sample"
})
public class VkCoarseSampleLocationNV extends VulkanStructure {
	public int pixelX;
	public int pixelY;
	public int sample;
}
