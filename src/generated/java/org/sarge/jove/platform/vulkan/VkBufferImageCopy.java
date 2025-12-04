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
public class VkBufferImageCopy implements NativeStructure {
	public long bufferOffset;
	public int bufferRowLength;
	public int bufferImageHeight;
	public VkImageSubresourceLayers imageSubresource;
	public VkOffset3D imageOffset;
	public VkExtent3D imageExtent;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_LONG.withName("bufferOffset"),
			JAVA_INT.withName("bufferRowLength"),
			JAVA_INT.withName("bufferImageHeight"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("aspectMask"),
				JAVA_INT.withName("mipLevel"),
				JAVA_INT.withName("baseArrayLayer"),
				JAVA_INT.withName("layerCount")
			).withName("imageSubresource"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("x"),
				JAVA_INT.withName("y"),
				JAVA_INT.withName("z")
			).withName("imageOffset"),
			PADDING,
			MemoryLayout.structLayout(
				JAVA_INT.withName("width"),
				JAVA_INT.withName("height"),
				JAVA_INT.withName("depth")
			).withName("imageExtent")
		);
	}
}
