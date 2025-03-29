package org.sarge.jove.platform.vulkan.memory;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeReference;
import org.sarge.jove.platform.vulkan.VkMemoryAllocateInfo;
import org.sarge.jove.platform.vulkan.common.DeviceContext;

/**
 * Vulkan memory API.
 * @author Sarge
 */
public interface MemoryLibrary {
	/**
	 * Allocates memory.
	 * @param device			Logical device
	 * @param pAllocateInfo		Memory descriptor
	 * @param pAllocator		Allocator
	 * @param pMemory			Returned memory handle
	 * @return Result
	 */
	int vkAllocateMemory(DeviceContext device, VkMemoryAllocateInfo pAllocateInfo, Handle pAllocator, NativeReference<Handle> pMemory);

	/**
	 * Releases memory.
	 * @param device			Logical device
	 * @param memory			Memory
	 * @param pAllocator		Allocator
	 */
	void vkFreeMemory(DeviceContext device, DeviceMemory memory, Handle pAllocator);

	/**
	 * Maps a region of memory.
	 * @param device			Logical device
	 * @param memory			Memory
	 * @param offset			Offset
	 * @param size				Data length
	 * @param flags				Flags (reserved)
	 * @param ppData			Returned pointer to the memory
	 * @return Result
	 */
	int vkMapMemory(DeviceContext device, DeviceMemory memory, long offset, long size, int flags, NativeReference<Handle> ppData);

	/**
	 * Unmaps a memory region.
	 * @param device			Logical device
	 * @param memory			Memory
	 */
	void vkUnmapMemory(DeviceContext device, DeviceMemory memory);
}
