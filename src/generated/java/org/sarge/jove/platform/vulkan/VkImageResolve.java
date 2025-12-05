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
public class VkImageResolve implements NativeStructure {
	public VkImageSubresourceLayers srcSubresource;
	public VkOffset3D srcOffset;
	public VkImageSubresourceLayers dstSubresource;
	public VkOffset3D dstOffset;
	public VkExtent3D extent;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			MemoryLayout.structLayout(
				JAVA_INT.withName("aspectMask"),
				JAVA_INT.withName("mipLevel"),
				JAVA_INT.withName("baseArrayLayer"),
				JAVA_INT.withName("layerCount")
			).withName("srcSubresource"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("x"),
				JAVA_INT.withName("y"),
				JAVA_INT.withName("z")
			).withName("srcOffset"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("aspectMask"),
				JAVA_INT.withName("mipLevel"),
				JAVA_INT.withName("baseArrayLayer"),
				JAVA_INT.withName("layerCount")
			).withName("dstSubresource"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("x"),
				JAVA_INT.withName("y"),
				JAVA_INT.withName("z")
			).withName("dstOffset"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("width"),
				JAVA_INT.withName("height"),
				JAVA_INT.withName("depth")
			).withName("extent")
		);
	}
}
