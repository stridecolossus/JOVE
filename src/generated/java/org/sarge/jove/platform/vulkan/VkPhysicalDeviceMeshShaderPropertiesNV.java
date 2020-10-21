package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"maxDrawMeshTasksCount",
	"maxTaskWorkGroupInvocations",
	"maxTaskWorkGroupSize",
	"maxTaskTotalMemorySize",
	"maxTaskOutputCount",
	"maxMeshWorkGroupInvocations",
	"maxMeshWorkGroupSize",
	"maxMeshTotalMemorySize",
	"maxMeshOutputVertices",
	"maxMeshOutputPrimitives",
	"maxMeshMultiviewViewCount",
	"meshOutputPerVertexGranularity",
	"meshOutputPerPrimitiveGranularity"
})
public class VkPhysicalDeviceMeshShaderPropertiesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceMeshShaderPropertiesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMeshShaderPropertiesNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MESH_SHADER_PROPERTIES_NV;
	public Pointer pNext;
	public int maxDrawMeshTasksCount;
	public int maxTaskWorkGroupInvocations;
	public int[] maxTaskWorkGroupSize = new int[3];
	public int maxTaskTotalMemorySize;
	public int maxTaskOutputCount;
	public int maxMeshWorkGroupInvocations;
	public int[] maxMeshWorkGroupSize = new int[3];
	public int maxMeshTotalMemorySize;
	public int maxMeshOutputVertices;
	public int maxMeshOutputPrimitives;
	public int maxMeshMultiviewViewCount;
	public int meshOutputPerVertexGranularity;
	public int meshOutputPerPrimitiveGranularity;
}
