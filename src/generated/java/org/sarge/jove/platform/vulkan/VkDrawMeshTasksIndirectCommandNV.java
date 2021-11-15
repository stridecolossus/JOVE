package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
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
	public static class ByValue extends VkDrawMeshTasksIndirectCommandNV implements Structure.ByValue { }
	public static class ByReference extends VkDrawMeshTasksIndirectCommandNV implements Structure.ByReference { }
	
	public int taskCount;
	public int firstTask;
}
