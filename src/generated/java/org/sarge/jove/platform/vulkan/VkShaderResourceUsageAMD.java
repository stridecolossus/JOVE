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
public class VkShaderResourceUsageAMD implements NativeStructure {
	public int numUsedVgprs;
	public int numUsedSgprs;
	public int ldsSizePerLocalWorkGroup;
	public long ldsUsageSizeInBytes;
	public long scratchMemUsageInBytes;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("numUsedVgprs"),
			JAVA_INT.withName("numUsedSgprs"),
			JAVA_INT.withName("ldsSizePerLocalWorkGroup"),
			PADDING,
			JAVA_LONG.withName("ldsUsageSizeInBytes"),
			JAVA_LONG.withName("scratchMemUsageInBytes")
		);
	}
}
