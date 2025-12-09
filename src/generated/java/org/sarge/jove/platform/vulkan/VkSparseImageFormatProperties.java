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
public class VkSparseImageFormatProperties implements NativeStructure {
	public EnumMask<VkImageAspectFlags> aspectMask;
	public VkExtent3D imageGranularity;
	public EnumMask<VkSparseImageFormatFlags> flags;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("aspectMask"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("width"),
				JAVA_INT.withName("height"),
				JAVA_INT.withName("depth")
			).withName("imageGranularity"),
			JAVA_INT.withName("flags")
		);
	}
}
