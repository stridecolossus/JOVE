package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"constantID",
	"offset",
	"size"
})
public class VkSpecializationMapEntry extends VulkanStructure {
	public int constantID;
	public int offset;
	public long size;
}
