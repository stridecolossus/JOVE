package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPushConstantRange implements NativeStructure {
	public EnumMask<VkShaderStage> stageFlags;
	public int offset;
	public int size;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("stageFlags"),
				JAVA_INT.withName("offset"),
				JAVA_INT.withName("size")
		);
	}
}
