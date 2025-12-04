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
public class VkDisplayPlaneCapabilities2KHR implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public VkDisplayPlaneCapabilitiesKHR capabilities;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("supportedAlpha"),
				PADDING,
				MemoryLayout.structLayout(
					JAVA_INT.withName("x"),
					JAVA_INT.withName("y")
				).withName("minSrcPosition"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("x"),
					JAVA_INT.withName("y")
				).withName("maxSrcPosition"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height")
				).withName("minSrcExtent"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height")
				).withName("maxSrcExtent"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("x"),
					JAVA_INT.withName("y")
				).withName("minDstPosition"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("x"),
					JAVA_INT.withName("y")
				).withName("maxDstPosition"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height")
				).withName("minDstExtent"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("width"),
					JAVA_INT.withName("height")
				).withName("maxDstExtent")
			).withName("capabilities")
		);
	}
}
