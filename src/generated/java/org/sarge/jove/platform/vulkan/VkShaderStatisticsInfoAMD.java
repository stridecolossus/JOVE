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
public class VkShaderStatisticsInfoAMD implements NativeStructure {
	public EnumMask<VkShaderStageFlags> shaderStageMask;
	public VkShaderResourceUsageAMD resourceUsage;
	public int numPhysicalVgprs;
	public int numPhysicalSgprs;
	public int numAvailableVgprs;
	public int numAvailableSgprs;
	public int[] computeWorkGroupSize;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("shaderStageMask"),
			PADDING,
			MemoryLayout.structLayout(
				JAVA_INT.withName("numUsedVgprs"),
				JAVA_INT.withName("numUsedSgprs"),
				JAVA_INT.withName("ldsSizePerLocalWorkGroup"),
				PADDING,
				JAVA_LONG.withName("ldsUsageSizeInBytes"),
				JAVA_LONG.withName("scratchMemUsageInBytes")
			).withName("resourceUsage"),
			JAVA_INT.withName("numPhysicalVgprs"),
			JAVA_INT.withName("numPhysicalSgprs"),
			JAVA_INT.withName("numAvailableVgprs"),
			JAVA_INT.withName("numAvailableSgprs"),
			MemoryLayout.sequenceLayout(3, JAVA_INT).withName("computeWorkGroupSize")
		);
	}
}
