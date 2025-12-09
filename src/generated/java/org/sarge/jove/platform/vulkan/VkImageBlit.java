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
public class VkImageBlit implements NativeStructure {
	public VkImageSubresourceLayers srcSubresource;
	public VkOffset3D[] srcOffsets;
	public VkImageSubresourceLayers dstSubresource;
	public VkOffset3D[] dstOffsets;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			MemoryLayout.structLayout(
				JAVA_INT.withName("aspectMask"),
				JAVA_INT.withName("mipLevel"),
				JAVA_INT.withName("baseArrayLayer"),
				JAVA_INT.withName("layerCount")
			).withName("srcSubresource"),
			MemoryLayout.sequenceLayout(2, MemoryLayout.structLayout(
				JAVA_INT.withName("x"),
				JAVA_INT.withName("y"),
				JAVA_INT.withName("z")
			)).withName("srcOffsets"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("aspectMask"),
				JAVA_INT.withName("mipLevel"),
				JAVA_INT.withName("baseArrayLayer"),
				JAVA_INT.withName("layerCount")
			).withName("dstSubresource"),
			MemoryLayout.sequenceLayout(2, MemoryLayout.structLayout(
				JAVA_INT.withName("x"),
				JAVA_INT.withName("y"),
				JAVA_INT.withName("z")
			)).withName("dstOffsets")
		);
	}
}
