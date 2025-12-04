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
public class VkClearAttachment implements NativeStructure {
	public EnumMask<VkImageAspectFlags> aspectMask;
	public int colorAttachment;
	public VkClearValue clearValue;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("aspectMask"),
			JAVA_INT.withName("colorAttachment"),
			MemoryLayout.unionLayout(
				MemoryLayout.unionLayout(
					MemoryLayout.sequenceLayout(4, JAVA_FLOAT).withName("float32"),
					MemoryLayout.sequenceLayout(4, JAVA_INT).withName("int32"),
					MemoryLayout.sequenceLayout(4, JAVA_INT).withName("uint32")
				).withName("color"),
				MemoryLayout.structLayout(
					JAVA_FLOAT.withName("depth"),
					JAVA_INT.withName("stencil")
				).withName("depthStencil")
			).withName("clearValue")
		);
	}
}
