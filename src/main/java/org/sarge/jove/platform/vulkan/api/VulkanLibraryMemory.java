package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkMemoryAllocateInfo;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan memory API.
 */
interface VulkanLibraryMemory {
	/**
	 * Allocates memory.
	 * @param device			Logical device
	 * @param pAllocateInfo		Memory descriptor
	 * @param pAllocator		Allocator
	 * @param pMemory			Returned memory handle
	 * @return Result code
	 */
	int vkAllocateMemory(Pointer device, VkMemoryAllocateInfo pAllocateInfo, Pointer pAllocator, PointerByReference pMemory);

	/**
	 * Releases memory.
	 * @param device			Logical device
	 * @param memory			Memory
	 * @param pAllocator		Allocator
	 */
	void vkFreeMemory(Pointer device, Pointer memory, Pointer pAllocator);

	/**
	 * Maps buffer memory.
	 * @param device			Logical device
	 * @param memory			Buffer memory
	 * @param offset			Offset
	 * @param size				Data length
	 * @param flags				Flags
	 * @param ppData			Returned pointer to the memory buffer
	 * @return Result code
	 */
	int vkMapMemory(Pointer device, Pointer memory, long offset, long size, int flags, PointerByReference ppData);

	/**
	 * Un-maps buffer memory.
	 * @param device			Logical device
	 * @param memory			Buffer memory
	 */
	void vkUnmapMemory(Pointer device, Pointer memory);
}