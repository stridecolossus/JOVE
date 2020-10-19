package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"depth",
	"stencil"
})
public class VkClearDepthStencilValue extends VulkanStructure { // implements ByReference  {
	public float depth;
	public int stencil;
}
