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
public class VkVertexInputBindingDescription implements NativeStructure {
	public int binding;
	public int stride;
	public VkVertexInputRate inputRate;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("binding"),
			JAVA_INT.withName("stride"),
			JAVA_INT.withName("inputRate")
		);
	}
}
