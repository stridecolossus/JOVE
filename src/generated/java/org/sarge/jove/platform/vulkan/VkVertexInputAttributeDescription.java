package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.ByReference;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"location",
	"binding",
	"format",
	"offset"
})
public class VkVertexInputAttributeDescription extends VulkanStructure implements ByReference {
	public int location;
	public int binding;
	public VkFormat format;
	public int offset;
}
