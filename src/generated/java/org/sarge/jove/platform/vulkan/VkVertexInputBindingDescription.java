package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.ByReference;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"binding",
	"stride",
	"inputRate"
})
public class VkVertexInputBindingDescription extends VulkanStructure implements ByReference {
	public int binding;
	public int stride;
	public VkVertexInputRate inputRate;
}
