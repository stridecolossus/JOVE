package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.ByReference;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"buffer",
	"offset",
	"range"
})
public class VkDescriptorBufferInfo extends VulkanStructure implements ByReference {
	public Handle buffer;
	public long offset;
	public long range;
}
