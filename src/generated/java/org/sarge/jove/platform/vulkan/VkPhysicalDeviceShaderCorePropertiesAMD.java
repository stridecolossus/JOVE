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
public class VkPhysicalDeviceShaderCorePropertiesAMD implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int shaderEngineCount;
	public int shaderArraysPerEngineCount;
	public int computeUnitsPerShaderArray;
	public int simdPerComputeUnit;
	public int wavefrontsPerSimd;
	public int wavefrontSize;
	public int sgprsPerSimd;
	public int minSgprAllocation;
	public int maxSgprAllocation;
	public int sgprAllocationGranularity;
	public int vgprsPerSimd;
	public int minVgprAllocation;
	public int maxVgprAllocation;
	public int vgprAllocationGranularity;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("shaderEngineCount"),
			JAVA_INT.withName("shaderArraysPerEngineCount"),
			JAVA_INT.withName("computeUnitsPerShaderArray"),
			JAVA_INT.withName("simdPerComputeUnit"),
			JAVA_INT.withName("wavefrontsPerSimd"),
			JAVA_INT.withName("wavefrontSize"),
			JAVA_INT.withName("sgprsPerSimd"),
			JAVA_INT.withName("minSgprAllocation"),
			JAVA_INT.withName("maxSgprAllocation"),
			JAVA_INT.withName("sgprAllocationGranularity"),
			JAVA_INT.withName("vgprsPerSimd"),
			JAVA_INT.withName("minVgprAllocation"),
			JAVA_INT.withName("maxVgprAllocation"),
			JAVA_INT.withName("vgprAllocationGranularity")
		);
	}
}
