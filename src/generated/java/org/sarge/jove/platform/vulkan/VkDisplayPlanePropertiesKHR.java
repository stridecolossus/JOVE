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
public class VkDisplayPlanePropertiesKHR implements NativeStructure {
	public Handle currentDisplay;
	public int currentStackIndex;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			POINTER.withName("currentDisplay"),
			JAVA_INT.withName("currentStackIndex"),
			PADDING
		);
	}
}
