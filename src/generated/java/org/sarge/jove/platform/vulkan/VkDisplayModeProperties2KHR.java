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
public class VkDisplayModeProperties2KHR implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public VkDisplayModePropertiesKHR displayModeProperties;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			MemoryLayout.structLayout(
				POINTER.withName("displayMode"),
				MemoryLayout.structLayout(
					MemoryLayout.structLayout(
						JAVA_INT.withName("width"),
						JAVA_INT.withName("height")
					).withName("visibleRegion"),
					JAVA_INT.withName("refreshRate")
				).withName("parameters")
			).withName("displayModeProperties")
		);
	}
}
