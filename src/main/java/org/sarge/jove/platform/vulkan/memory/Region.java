package org.sarge.jove.platform.vulkan.memory;

import java.nio.ByteBuffer;

import org.sarge.jove.platform.vulkan.VkMemoryProperty;

/**
 * A <i>region</i> is a mapped area of {@link VkMemoryProperty#HOST_VISIBLE} memory.
 * @see DeviceMemory#map()
 * @author Sarge
 */
public interface Region {
	/**
	 * @return Size of this region (bytes)
	 */
	long size();

	/**
	 * Provides an NIO buffer to access a sub-section of this memory region.
	 * @param offset		Offset
	 * @param size			Region size (bytes)
	 * @return Buffer
	 * @throws IllegalArgumentException if the {@code offset} and {@code size} exceeds the size of this region
	 * @throws IllegalStateException if this region has been released or the memory has been destroyed
	 */
	ByteBuffer buffer(long offset, long size);

	/**
	 * Unmaps this region.
	 * @throws IllegalStateException if the mapping has already been released or the memory has been destroyed
	 */
	void unmap();
}
