package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"indexCount",
	"instanceCount",
	"firstIndex",
	"vertexOffset",
	"firstInstance"
})
public class VkDrawIndexedIndirectCommand extends VulkanStructure {
	public int indexCount;
	public int instanceCount;
	public int firstIndex;
	public int vertexOffset;
	public int firstInstance;
}
