package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkFormatProperties implements NativeStructure {
	public EnumMask<VkFormatFeature> linearTilingFeatures;
	public EnumMask<VkFormatFeature> optimalTilingFeatures;
	public EnumMask<VkFormatFeature> bufferFeatures;

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("linearTilingFeatures"),
				JAVA_INT.withName("optimalTilingFeatures"),
				JAVA_INT.withName("bufferFeatures")
		);
	}
}
