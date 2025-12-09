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
public class VkImageSubresource implements NativeStructure {
	public EnumMask<VkImageAspectFlags> aspectMask;
	public int mipLevel;
	public int arrayLayer;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("aspectMask"),
			JAVA_INT.withName("mipLevel"),
			JAVA_INT.withName("arrayLayer")
		);
	}
}
