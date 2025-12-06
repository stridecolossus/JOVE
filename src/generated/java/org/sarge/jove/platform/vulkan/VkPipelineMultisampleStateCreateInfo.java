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
public class VkPipelineMultisampleStateCreateInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int flags;
	public EnumMask<VkSampleCountFlags> rasterizationSamples;
	public boolean sampleShadingEnable;
	public float minSampleShading;
	public int[] pSampleMask;
	public boolean alphaToCoverageEnable;
	public boolean alphaToOneEnable;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("flags"),
			JAVA_INT.withName("rasterizationSamples"),
			JAVA_INT.withName("sampleShadingEnable"),
			JAVA_FLOAT.withName("minSampleShading"),
			POINTER.withName("pSampleMask"),
			JAVA_INT.withName("alphaToCoverageEnable"),
			JAVA_INT.withName("alphaToOneEnable")
		);
	}
}
