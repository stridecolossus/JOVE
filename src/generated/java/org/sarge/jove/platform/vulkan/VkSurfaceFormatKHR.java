package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSurfaceFormatKHR implements NativeStructure {
	public VkFormat format;
	public VkColorSpaceKHR colorSpace;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("format"),
				JAVA_INT.withName("colorSpace")
		);
	}
}
