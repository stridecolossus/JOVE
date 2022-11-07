package org.sarge.jove.platform.vulkan.memory;

import java.nio.ByteBuffer;

/**
 * A <i>region</i> is a mapped area of host visible memory.
 * @see DeviceMemory#map()
 * @author Sarge
 */
public interface Region {
	/**
	 * @return Size of this region (bytes)
	 */
	long size();

	/**
	 * Provides a byte-buffer to access a sub-section of this memory region.
	 * @param offset		Offset
	 * @param size			Region size (bytes)
	 * @return Byte-buffer
	 * @throws IllegalArgumentException if the {@code offset} and {@code size} exceeds the size of this region
	 * @throws IllegalStateException if this region has been released or the memory has been destroyed
	 */
	ByteBuffer buffer(long offset, long size);

	/**
	 * Provides a byte-buffer to access this memory region.
	 * @return Byte-buffer
	 * @throws IllegalStateException if this region has been released or the memory has been destroyed
	 */
	default ByteBuffer buffer() {
		return buffer(0, size());
	}

	/**
	 * Un-maps this mapped region.
	 * @throws IllegalStateException if the mapping has already been released or the memory has been destroyed
	 */
	void unmap();
}
