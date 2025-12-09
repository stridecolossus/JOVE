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
public class VkSubresourceLayout implements NativeStructure {
	public long offset;
	public long size;
	public long rowPitch;
	public long arrayPitch;
	public long depthPitch;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_LONG.withName("offset"),
			JAVA_LONG.withName("size"),
			JAVA_LONG.withName("rowPitch"),
			JAVA_LONG.withName("arrayPitch"),
			JAVA_LONG.withName("depthPitch")
		);
	}
}
