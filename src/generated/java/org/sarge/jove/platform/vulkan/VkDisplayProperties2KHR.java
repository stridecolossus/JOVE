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
public class VkDisplayProperties2KHR implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public VkDisplayPropertiesKHR displayProperties;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			MemoryLayout.structLayout(
				POINTER.withName("display"),
				POINTER.withName("displayName"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height")
				).withName("physicalDimensions"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height")
				).withName("physicalResolution"),
				JAVA_INT.withName("supportedTransforms"),
				JAVA_INT.withName("planeReorderPossible"),
				JAVA_INT.withName("persistentContent"),
				PADDING
			).withName("displayProperties")
		);
	}
}
