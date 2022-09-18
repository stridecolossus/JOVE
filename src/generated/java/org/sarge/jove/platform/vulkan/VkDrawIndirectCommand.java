package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"vertexCount",
	"instanceCount",
	"firstVertex",
	"firstInstance"
})
public class VkDrawIndirectCommand extends VulkanStructure {
	public int vertexCount;
	public int instanceCount;
	public int firstVertex;
	public int firstInstance;
}
