package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"taskCount",
	"firstTask"
})
public class VkDrawMeshTasksIndirectCommandNV extends VulkanStructure {
	public int taskCount;
	public int firstTask;
}
