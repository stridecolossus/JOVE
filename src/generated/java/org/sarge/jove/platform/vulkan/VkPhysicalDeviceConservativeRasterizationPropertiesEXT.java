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
public class VkPhysicalDeviceConservativeRasterizationPropertiesEXT implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public float primitiveOverestimationSize;
	public float maxExtraPrimitiveOverestimationSize;
	public float extraPrimitiveOverestimationSizeGranularity;
	public boolean primitiveUnderestimation;
	public boolean conservativePointAndLineRasterization;
	public boolean degenerateTrianglesRasterized;
	public boolean degenerateLinesRasterized;
	public boolean fullyCoveredFragmentShaderInputVariable;
	public boolean conservativeRasterizationPostDepthCoverage;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_FLOAT.withName("primitiveOverestimationSize"),
			JAVA_FLOAT.withName("maxExtraPrimitiveOverestimationSize"),
			JAVA_FLOAT.withName("extraPrimitiveOverestimationSizeGranularity"),
			JAVA_INT.withName("primitiveUnderestimation"),
			JAVA_INT.withName("conservativePointAndLineRasterization"),
			JAVA_INT.withName("degenerateTrianglesRasterized"),
			JAVA_INT.withName("degenerateLinesRasterized"),
			JAVA_INT.withName("fullyCoveredFragmentShaderInputVariable"),
			JAVA_INT.withName("conservativeRasterizationPostDepthCoverage")
		);
	}
}
