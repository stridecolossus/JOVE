package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

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
public class VkPhysicalDeviceMeshShaderPropertiesNV extends Structure {
	public static class ByValue extends VkPhysicalDeviceMeshShaderPropertiesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMeshShaderPropertiesNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MESH_SHADER_PROPERTIES_NV.value();
	public Pointer pNext;
	public int maxDrawMeshTasksCount;
	public int maxTaskWorkGroupInvocations;
	public final int[] maxTaskWorkGroupSize = new int[3];
	public int maxTaskTotalMemorySize;
	public int maxTaskOutputCount;
	public int maxMeshWorkGroupInvocations;
	public final int[] maxMeshWorkGroupSize = new int[3];
	public int maxMeshTotalMemorySize;
	public int maxMeshOutputVertices;
	public int maxMeshOutputPrimitives;
	public int maxMeshMultiviewViewCount;
	public int meshOutputPerVertexGranularity;
	public int meshOutputPerPrimitiveGranularity;
}
