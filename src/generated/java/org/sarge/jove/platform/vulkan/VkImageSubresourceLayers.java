package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkImageSubresourceLayers implements NativeStructure {
	public EnumMask<VkImageAspect> aspectMask;
	public int mipLevel;
	public int baseArrayLayer;
	public int layerCount;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("aspectMask"),
				JAVA_INT.withName("mipLevel"),
				JAVA_INT.withName("baseArrayLayer"),
				JAVA_INT.withName("layerCount")
		);
	}
}
