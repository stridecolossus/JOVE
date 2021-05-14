package org.sarge.jove.platform.vulkan.memory;

import org.sarge.jove.common.ByteSource.Sink;
import org.sarge.jove.common.TransientNativeObject;

/**
 * A <i>device memory</i> is an area of device or host memory accessible to the hardware.
 * <p>
 * A <i>region</i> of the memory must be <i>mapped</i> using {@link #map(long, long)} in order to perform read or write access.
 * <p>
 * Notes:
 * <ul>
 * <li>Only <b>one</b> active mapping is permitted on a given instance at any one time</li>
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
 * Sink region = mem.map();
 * region.write(new byte[]{...});
 *
 * // Un-map memory region
 * mem.unmap();
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
	Sink map(long size, long offset);

	/**
	 * Maps this device memory.
	 * @return Mapped memory
	 * @throws IllegalStateException if a mapping already exists or this memory has been destroyed
	 * @see #map(long, long)
	 */
	default Sink map() {
		return map(size(), 0);
	}

	/**
	 * Releases the currently mapped region of this memory.
	 * @throws IllegalStateException if this memory has been destroyed or is not mapped
	 */
	void unmap();
}
