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
public class VkDrawIndirectCommand implements NativeStructure {
	public int vertexCount;
	public int instanceCount;
	public int firstVertex;
	public int firstInstance;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("vertexCount"),
			JAVA_INT.withName("instanceCount"),
			JAVA_INT.withName("firstVertex"),
			JAVA_INT.withName("firstInstance")
		);
	}
}
