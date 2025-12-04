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
public class VkDisplayPlaneCapabilitiesKHR implements NativeStructure {
	public EnumMask<VkDisplayPlaneAlphaFlagsKHR> supportedAlpha;
	public VkOffset2D minSrcPosition;
	public VkOffset2D maxSrcPosition;
	public VkExtent2D minSrcExtent;
	public VkExtent2D maxSrcExtent;
	public VkOffset2D minDstPosition;
	public VkOffset2D maxDstPosition;
	public VkExtent2D minDstExtent;
	public VkExtent2D maxDstExtent;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
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
		);
	}
}
