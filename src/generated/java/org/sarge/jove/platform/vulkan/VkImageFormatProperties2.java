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
public class VkImageFormatProperties2 implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public VkImageFormatProperties imageFormatProperties;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			MemoryLayout.structLayout(
				MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height"),
					JAVA_INT.withName("depth")
				).withName("maxExtent"),
				JAVA_INT.withName("maxMipLevels"),
				JAVA_INT.withName("maxArrayLayers"),
				JAVA_INT.withName("sampleCounts"),
				JAVA_LONG.withName("maxResourceSize")
			).withName("imageFormatProperties")
		);
	}
}
