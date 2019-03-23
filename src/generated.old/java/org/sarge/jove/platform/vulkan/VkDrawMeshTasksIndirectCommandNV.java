package org.sarge.jove.platform.vulkan;

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
public class VkDrawMeshTasksIndirectCommandNV extends Structure {
	public static class ByValue extends VkDrawMeshTasksIndirectCommandNV implements Structure.ByValue { }
	public static class ByReference extends VkDrawMeshTasksIndirectCommandNV implements Structure.ByReference { }
	
	public int taskCount;
	public int firstTask;
}
