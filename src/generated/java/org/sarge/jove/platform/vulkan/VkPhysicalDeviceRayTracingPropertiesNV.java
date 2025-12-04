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
public class VkPhysicalDeviceRayTracingPropertiesNV implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int shaderGroupHandleSize;
	public int maxRecursionDepth;
	public int maxShaderGroupStride;
	public int shaderGroupBaseAlignment;
	public long maxGeometryCount;
	public long maxInstanceCount;
	public long maxTriangleCount;
	public int maxDescriptorSetAccelerationStructures;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("shaderGroupHandleSize"),
			JAVA_INT.withName("maxRecursionDepth"),
			JAVA_INT.withName("maxShaderGroupStride"),
			JAVA_INT.withName("shaderGroupBaseAlignment"),
			JAVA_LONG.withName("maxGeometryCount"),
			JAVA_LONG.withName("maxInstanceCount"),
			JAVA_LONG.withName("maxTriangleCount"),
			JAVA_INT.withName("maxDescriptorSetAccelerationStructures")
		);
	}
}
