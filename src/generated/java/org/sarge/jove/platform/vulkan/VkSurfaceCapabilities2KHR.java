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
public class VkSurfaceCapabilities2KHR implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public VkSurfaceCapabilitiesKHR surfaceCapabilities;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("minImageCount"),
				JAVA_INT.withName("maxImageCount"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height")
				).withName("currentExtent"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height")
				).withName("minImageExtent"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height")
				).withName("maxImageExtent"),
				JAVA_INT.withName("maxImageArrayLayers"),
				JAVA_INT.withName("supportedTransforms"),
				JAVA_INT.withName("currentTransform"),
				JAVA_INT.withName("supportedCompositeAlpha"),
				JAVA_INT.withName("supportedUsageFlags")
			).withName("surfaceCapabilities"),
			PADDING
		);
	}
}
