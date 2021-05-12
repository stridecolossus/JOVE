package org.sarge.jove.platform.vulkan.memory;

import java.nio.ByteBuffer;

import org.sarge.jove.common.TransientNativeObject;

/**
 * A <i>device memory</i> is a region of memory allocated by Vulkan.
 * <p>
 * A <i>region</i> of the memory must be <i>mapped</i> using {@link #map(long, long)} in order to perform read or write access.
 * <p>
 * Notes:
 * <ul>
 * <li>Only <b>one</b> active mapping is permitted on a given device memory instance at any one time</li>
 * <li>Memory mappings can be <i>persistent</i>, i.e. it is not required to explicitly un-map memory after a read/write access</li>
 * <li>Memory can be assumed to be automatically un-mapped when it is released</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * // Create memory record
 * DeviceMemory mem = new DefaultDeviceMemory(handle, dev, size);
 *
 * // Write to memory
 * MappedRegion region = mem.map();
 * region.write(new byte[]{...});
 *
 * // Un-map memory region
 * region.unmap();
 *
 * // Release memory
 * mem.destroy();
 * </pre>
 * @author Sarge
 */
public interface DeviceMemory extends TransientNativeObject {
	/**
	 * @return Size of this memory (bytes)
	 */
	long size();

	/**
	 * @return Whether this memory has been mapped
	 */
	boolean isMapped();

	/**
	 * Maps a region of this device memory.
	 * @param size			Size of the region to map
	 * @param offset		Offset into this memory
	 * @return Mapped memory
	 * @throws IllegalStateException if a mapping already exists or this memory has been destroyed
	 */
	MappedRegion map(long size, long offset);

	/**
	 * Maps this device memory.
	 * @return Mapped memory
	 * @throws IllegalStateException if a mapping already exists or this memory has been destroyed
	 * @see #map(long, long)
	 */
	default MappedRegion map() {
		return map(size(), 0);
	}

	/**
	 * A <i>mapped region</i> is a segment of device memory accessible for writing.
	 * @see DeviceMemory#map()
	 */
	interface MappedRegion {
		/**
		 * Writes the given byte array to this region.
		 * @param array Array to write
		 * @throws IllegalStateException if the region has been invalidated or the memory has been destroyed
		 * @throws IllegalArgumentException if the given array is larger than this memory
		 */
		void write(byte[] array);

		/**
		 * Writes the given byte buffer to this region.
		 * @param buffer Buffer to write
		 * @throws IllegalStateException if the region has been invalidated or the memory has been destroyed
		 * @throws IllegalArgumentException if the size of the remaining data in the given buffer is larger than this region
		 */
		void write(ByteBuffer buffer);

		/**
		 * Invalidates this region.
		 * Note that a region is automatically invalidated if the memory is released.
		 */
		void unmap();
	}
}
