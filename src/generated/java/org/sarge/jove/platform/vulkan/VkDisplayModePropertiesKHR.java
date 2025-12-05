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
public class VkDisplayModePropertiesKHR implements NativeStructure {
	public Handle displayMode;
	public VkDisplayModeParametersKHR parameters;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			POINTER.withName("displayMode"),
			MemoryLayout.structLayout(
				MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height")
				).withName("visibleRegion"),
				JAVA_INT.withName("refreshRate")
			).withName("parameters"),
			PADDING
		);
	}
}
