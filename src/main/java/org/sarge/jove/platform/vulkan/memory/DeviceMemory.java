package org.sarge.jove.platform.vulkan.memory;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.sarge.jove.common.*;

/**
 * A <i>device memory</i> instance is an area of device or host memory accessible to the hardware.
 * <p>
 * A <i>region</i> of the memory must be <i>mapped</i> using {@link #map(long, long)} in order to perform read or write access.
 * <p>
 * Notes:
 * <ul>
 * <li>Only <b>one</b> active mapping is permitted on a given instance at any one time</li>
 * <li>Memory mappings can be <i>persistent</i>, i.e. it is not required to explicitly un-map memory after a read/write access</li>
 * <li>Memory can be assumed to be automatically un-mapped when it is released</li>
 * <li>Buffers retrieved from a mapped region using {@link Region#buffer(long, long)} are invalidated if the region is un-mapped or the memory destroyed</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * // Create memory record
 * DeviceMemory mem = ...
 *
 * // Map accessible region
 * Region region = mem.region(0, size);
 *
 * // Write to memory
 * ByteBuffer buffer = region.buffer();
 * buffer.put(...);
 *
 * // Release mapping
 * region.unmap();
 *
 * // Release memory
 * mem.destroy();
 * </pre>
 * @author Sarge
 */
public interface DeviceMemory extends NativeObject, TransientObject {
	/**
	 * A <i>region</i> is a mapped area of device memory.
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

	/**
	 * @return Size of this memory (bytes)
	 */
	long size();

	/**
	 * @return Mapped memory region
	 */
	Optional<Region> region();

	/**
	 * Maps a region of this device memory.
	 * @param offset		Offset into this memory
	 * @param size			Size of the region to map
	 * @return Mapped memory region
	 * @throws IllegalArgumentException if the {@code offset} and {@code size} exceeds the size of this memory
	 * @throws IllegalStateException if a mapping already exists or this memory has been destroyed
	 */
	Region map(long offset, long size);

	/**
	 * Maps this device memory.
	 * @return Mapped memory region
	 * @throws IllegalStateException if a mapping already exists or this memory has been destroyed
	 */
	default Region map() {
		return map(0, size());
	}
}
