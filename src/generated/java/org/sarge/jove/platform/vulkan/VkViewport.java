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
public class VkViewport implements NativeStructure {
	public float x;
	public float y;
	public float width;
	public float height;
	public float minDepth;
	public float maxDepth;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_FLOAT.withName("x"),
			JAVA_FLOAT.withName("y"),
			JAVA_FLOAT.withName("width"),
			JAVA_FLOAT.withName("height"),
			JAVA_FLOAT.withName("minDepth"),
			JAVA_FLOAT.withName("maxDepth")
		);
	}
}
