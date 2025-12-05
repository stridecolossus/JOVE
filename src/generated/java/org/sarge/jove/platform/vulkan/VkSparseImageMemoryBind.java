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
public class VkSparseImageMemoryBind implements NativeStructure {
	public VkImageSubresource subresource;
	public VkOffset3D offset;
	public VkExtent3D extent;
	public Handle memory;
	public long memoryOffset;
	public EnumMask<VkSparseMemoryBindFlags> flags;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			MemoryLayout.structLayout(
				JAVA_INT.withName("aspectMask"),
				JAVA_INT.withName("mipLevel"),
				JAVA_INT.withName("arrayLayer")
			).withName("subresource"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("x"),
				JAVA_INT.withName("y"),
				JAVA_INT.withName("z")
			).withName("offset"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("width"),
				JAVA_INT.withName("height"),
				JAVA_INT.withName("depth")
			).withName("extent"),
			PADDING,
			POINTER.withName("memory"),
			JAVA_LONG.withName("memoryOffset"),
			JAVA_INT.withName("flags"),
			PADDING
		);
	}
}
