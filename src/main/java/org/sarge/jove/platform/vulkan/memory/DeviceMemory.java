package org.sarge.jove.platform.vulkan.memory;

import java.util.Optional;
import java.util.function.Predicate;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlags;

/**
 * A <i>device memory</i> instance is an area of memory accessible to the hardware.
 * <p>
 * Memory that is {@link VkMemoryPropertyFlags#HOST_VISIBLE} can be <i>mapped</i> using {@link #map(long, long)} in order to perform read or write operations by the application.
 * <p>
 * Notes:
 * <ul>
 * <li>Only <b>one</b> active mapping is permitted on a given instance at any one time</li>
 * <li>Memory mappings can be <i>persistent</i>, i.e. it is not required to explicitly unmap memory after a read/write access</li>
 * <li>Memory can be assumed to be automatically unmapped when it is released</li>
 * <li>A mapped region is invalidated if the region is unmapped or the memory has been destroyed</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * // Create memory record
 * DeviceMemory memory = ...
 *
 * // Map accessible region
 * Region region = memory.map();
 *
 * // Write to memory
 * MemorySegment address = memory.segment();
 * ByteBuffer buffer = address.asByteBuffer();
 * ...
 *
 * // Release mapping
 * region.unmap();
 *
 * // Release memory
 * memory.destroy();
 * </pre>
 * <p>
 * @author Sarge
 */
public interface DeviceMemory extends NativeObject, TransientObject {
	/**
	 * Active memory filter.
	 */
	Predicate<DeviceMemory> ALIVE = Predicate.not(DeviceMemory::isDestroyed);

	/**
	 * @return Type of this memory
	 */
	MemoryType type();

	/**
	 * @return Size of this memory (bytes)
	 */
	long size();

	/**
	 * @return Mapped memory region
	 */
	Optional<Region> region();

	/**
	 * Maps a segment of this device memory.
	 * @param offset		Offset into this memory
	 * @param size			Size of the region to map
	 * @return Mapped memory region
	 * @throws IllegalArgumentException if {@code offset} and {@code size} exceed the size of this memory
	 * @throws IllegalStateException if this memory is not {@link VkMemoryProperty#HOST_VISIBLE}, a mapping already exists, or the memory has been destroyed
	 */
	Region map(long offset, long size);

	/**
	 * Maps this entire memory block.
	 * @return Mapping memory
	 * @throws IllegalStateException if this memory is not {@link VkMemoryProperty#HOST_VISIBLE}, a mapping already exists, or the memory has been destroyed
	 * @see #map(long, long)
	 */
	default Region map() {
		return map(0L, size());
	}
}
