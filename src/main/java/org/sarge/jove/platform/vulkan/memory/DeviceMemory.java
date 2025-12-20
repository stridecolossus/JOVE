package org.sarge.jove.platform.vulkan.memory;

import java.lang.foreign.MemorySegment;
import java.util.Optional;
import java.util.function.Predicate;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlags;

/**
 * A <i>device memory</i> instance is an area of memory accessible to the hardware.
 * <p>
 * Memory that is {@link VkMemoryPropertyFlags#HOST_VISIBLE} can be <i>mapped</i> using {@link #map(long, long)} in order to perform read or write operations by the application.
 * <p>
 * Usage:
 * <pre>
 * // Create memory record
 * DeviceMemory memory = ...
 *
 * // Map accessible region
 * MemorySegment region = memory.map();
 *
 * // Write to memory
 * ByteBuffer buffer = address.asByteBuffer();
 * ...
 *
 * // Release mapping
 * memory.unmap();
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
	Optional<MemorySegment> region();

	/**
	 * Maps a <i>region</i> of this device memory.
	 * Only <b>one</b> region can be active for a given device memory instance.
	 * The region is invalidated if the memory is destroyed.
	 * @param offset		Offset into this memory
	 * @param size			Size of the region to map
	 * @return Mapped memory region
	 * @throws IllegalArgumentException if {@code offset} and {@code size} exceed the size of this memory
	 * @throws IllegalStateException if this memory is not {@link VkMemoryProperty#HOST_VISIBLE}, a mapping already exists, or the memory has been destroyed
	 * @see #unmap()
	 */
	MemorySegment map(long offset, long size);

	/**
	 * Maps this entire memory block.
	 * @return Mapping memory
	 * @throws IllegalStateException if this memory is not {@link VkMemoryProperty#HOST_VISIBLE}, a mapping already exists, or the memory has been destroyed
	 * @see #map(long, long)
	 * @see #unmap()
	 */
	default MemorySegment map() {
		return map(0L, size());
	}

	/**
	 * Unmaps the current mapped region of this memory.
	 * Note that regions are <i>persistent</i>, i.e. it is not required to explicitly unmap memory after a read/write access.
	 * Similarly a region does not need to be explicitly unmapped when the memory is destroyed.
	 * @throws IllegalStateException if this memory has not been mapped or the memory has been destroyed
	 * @see #map()
	 */
	void unmap();
}
