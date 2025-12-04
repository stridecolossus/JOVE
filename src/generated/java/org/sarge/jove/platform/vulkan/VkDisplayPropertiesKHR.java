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
public class VkDisplayPropertiesKHR implements NativeStructure {
	public Handle display;
	public String displayName;
	public VkExtent2D physicalDimensions;
	public VkExtent2D physicalResolution;
	public EnumMask<VkSurfaceTransformFlagsKHR> supportedTransforms;
	public boolean planeReorderPossible;
	public boolean persistentContent;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
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
			JAVA_INT.withName("persistentContent")
		);
	}
}
