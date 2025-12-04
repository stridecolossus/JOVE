package org.sarge.jove.platform.vulkan.memory;

import java.lang.foreign.MemorySegment;

import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlags;

/**
 * A <i>region</i> is a mapped area of {@link VkMemoryPropertyFlags#HOST_VISIBLE} memory.
 * @see DeviceMemory#map()
 * @author Sarge
 */
public interface Region {
	/**
	 * @return Size of this region (bytes)
	 */
	long size();

	/**
	 * @return Region memory
	 * @throws IllegalStateException if the mapping has already been released or the memory has been destroyed
	 */
	MemorySegment memory();

	/**
	 * Unmaps this region.
	 * @throws IllegalStateException if the mapping has already been released or the memory has been destroyed
	 */
	void unmap();
}
