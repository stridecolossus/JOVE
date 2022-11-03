package org.sarge.jove.platform.vulkan.memory;

import org.sarge.jove.platform.vulkan.VkMemoryAllocateInfo;
import org.sarge.jove.platform.vulkan.common.DeviceContext;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan memory API.
 */
public interface MemoryLibrary {
	/**
	 * Allocates memory.
	 * @param device			Logical device
	 * @param pAllocateInfo		Memory descriptor
	 * @param pAllocator		Allocator
	 * @param pMemory			Returned memory
	 * @return Result
	 */
	int vkAllocateMemory(DeviceContext device, VkMemoryAllocateInfo pAllocateInfo, Pointer pAllocator, PointerByReference pMemory);

	/**
	 * Releases memory.
	 * @param device			Logical device
	 * @param memory			Memory
	 * @param pAllocator		Allocator
	 */
	void vkFreeMemory(DeviceContext device, DeviceMemory memory, Pointer pAllocator);

	/**
	 * Maps buffer memory.
	 * @param device			Logical device
	 * @param memory			Buffer memory
	 * @param offset			Offset
	 * @param size				Data length
	 * @param flags				Flags (reserved)
	 * @param ppData			Returned pointer to the memory buffer
	 * @return Result
	 */
	int vkMapMemory(DeviceContext device, DeviceMemory memory, long offset, long size, int flags, PointerByReference ppData);

	/**
	 * Un-maps buffer memory.
	 * @param device			Logical device
	 * @param memory			Buffer memory
	 */
	void vkUnmapMemory(DeviceContext device, DeviceMemory memory);
}
