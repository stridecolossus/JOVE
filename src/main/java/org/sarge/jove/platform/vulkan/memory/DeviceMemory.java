package org.sarge.jove.platform.vulkan.memory;

import java.util.Optional;

import org.sarge.jove.common.*;

/**
 * A <i>device memory</i> instance is an area of device or host memory accessible to the hardware.
 * <p>
 * Device memory that {@link #isHostVisible()} can be <i>mapped</i> using {@link #map(long, long)} in order to perform read or write operations.
 * See {@link MemoryType#isHostVisible()}
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
 * Region region = mem.map();
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
 * <p>
 * @author Sarge
 */
public interface DeviceMemory extends NativeObject, TransientObject {
	/**
	 * @return Size of this memory (bytes)
	 */
	long size();

	/**
	 * @return Whether this memory is host visible
	 * @see #map()
	 * @see MemoryType#isHostVisible()
	 */
	default boolean isHostVisible() {
		return true;
	}

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
	 * @throws IllegalStateException if this memory is not {@link #isHostVisible()}, a mapping already exists, or the memory has been destroyed
	 */
	Region map(long offset, long size);

	/**
	 * Maps this device memory.
	 * @return Mapped memory region
	 * @throws IllegalStateException if this memory is not {@link #isHostVisible()}, a mapping already exists, or the memory has been destroyed
	 */
	default Region map() {
		return map(0, size());
	}

	/**
	 * Reallocates this memory.
	 * @throws IllegalStateException if this memory cannot be reallocated
	 * @throws UnsupportedOperationException by default
	 */
	default DeviceMemory reallocate() {
		throw new UnsupportedOperationException();
	}
}
