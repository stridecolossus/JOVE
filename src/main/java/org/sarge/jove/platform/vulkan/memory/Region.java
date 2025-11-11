package org.sarge.jove.platform.vulkan.memory;

import java.lang.foreign.MemorySegment;

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
	 * Accesses a segment of this region.
	 * @param offset		Offset
	 * @param size			Segment size (bytes)
	 * @return Memory segment
	 * @throws IndexOutOfBoundsException if the {@link #offset} or {@link #size} are invalid for this region
	 */
	MemorySegment segment(long offset, long size);

	/**
	 * Unmaps this region.
	 * @throws IllegalStateException if the mapping has already been released or the memory has been destroyed
	 */
	void unmap();
}
