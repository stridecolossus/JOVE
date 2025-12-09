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
public class VkFormatProperties implements NativeStructure {
	public EnumMask<VkFormatFeatureFlags> linearTilingFeatures;
	public EnumMask<VkFormatFeatureFlags> optimalTilingFeatures;
	public EnumMask<VkFormatFeatureFlags> bufferFeatures;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("linearTilingFeatures"),
			JAVA_INT.withName("optimalTilingFeatures"),
			JAVA_INT.withName("bufferFeatures")
		);
	}
}
