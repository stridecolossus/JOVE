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
public class VkSparseImageMemoryRequirements implements NativeStructure {
	public VkSparseImageFormatProperties formatProperties;
	public int imageMipTailFirstLod;
	public long imageMipTailSize;
	public long imageMipTailOffset;
	public long imageMipTailStride;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			MemoryLayout.structLayout(
				JAVA_INT.withName("aspectMask"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height"),
					JAVA_INT.withName("depth")
				).withName("imageGranularity"),
				JAVA_INT.withName("flags")
			).withName("formatProperties"),
			JAVA_INT.withName("imageMipTailFirstLod"),
			JAVA_LONG.withName("imageMipTailSize"),
			JAVA_LONG.withName("imageMipTailOffset"),
			JAVA_LONG.withName("imageMipTailStride")
		);
	}
}
