package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.common.Handle;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.platform.vulkan.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPhysicalDeviceMeshShaderPropertiesNV implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int maxDrawMeshTasksCount;
	public int maxTaskWorkGroupInvocations;
	public int[] maxTaskWorkGroupSize;
	public int maxTaskTotalMemorySize;
	public int maxTaskOutputCount;
	public int maxMeshWorkGroupInvocations;
	public int[] maxMeshWorkGroupSize;
	public int maxMeshTotalMemorySize;
	public int maxMeshOutputVertices;
	public int maxMeshOutputPrimitives;
	public int maxMeshMultiviewViewCount;
	public int meshOutputPerVertexGranularity;
	public int meshOutputPerPrimitiveGranularity;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("maxDrawMeshTasksCount"),
			JAVA_INT.withName("maxTaskWorkGroupInvocations"),
			MemoryLayout.sequenceLayout(3, JAVA_INT).withName("maxTaskWorkGroupSize"),
			JAVA_INT.withName("maxTaskTotalMemorySize"),
			JAVA_INT.withName("maxTaskOutputCount"),
			JAVA_INT.withName("maxMeshWorkGroupInvocations"),
			MemoryLayout.sequenceLayout(3, JAVA_INT).withName("maxMeshWorkGroupSize"),
			JAVA_INT.withName("maxMeshTotalMemorySize"),
			JAVA_INT.withName("maxMeshOutputVertices"),
			JAVA_INT.withName("maxMeshOutputPrimitives"),
			JAVA_INT.withName("maxMeshMultiviewViewCount"),
			JAVA_INT.withName("meshOutputPerVertexGranularity"),
			JAVA_INT.withName("meshOutputPerPrimitiveGranularity")
		);
	}
}
