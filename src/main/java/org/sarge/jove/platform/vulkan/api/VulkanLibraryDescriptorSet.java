package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkCopyDescriptorSet;
import org.sarge.jove.platform.vulkan.VkDescriptorPoolCreateInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorSetAllocateInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkWriteDescriptorSet;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Descriptor sets API.
 */
interface VulkanLibraryDescriptorSet {
	/**
	 * Creates a descriptor set layout.
	 * @param device				Logical device
	 * @param pCreateInfo			Create descriptor
	 * @param pAllocator			Allocator
	 * @param pSetLayout			Returned layout handle
	 * @return Result code
	 */
	int vkCreateDescriptorSetLayout(Handle device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Handle pAllocator, PointerByReference pSetLayout);

	/**
	 * Destroys a descriptor set layout.
	 * @param device				Logical device
	 * @param descriptorSetLayout	Layout
	 * @param pAllocator			Allocator
	 */
	void vkDestroyDescriptorSetLayout(Handle device, Handle descriptorSetLayout, Handle pAllocator);

	/**
	 * Creates a descriptor-set pool.
	 * @param device				Logical device
	 * @param pCreateInfo			Descriptor
	 * @param pAllocator			Allocator
	 * @param pDescriptorPool		Returned pool
	 * @return Result code
	 */
	int vkCreateDescriptorPool(Handle device, VkDescriptorPoolCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pDescriptorPool);

	/**
	 * Destroys a descriptor-set pool.
	 * @param device				Logical device
	 * @param descriptorPool		Pool
	 * @param pAllocator			Allocator
	 */
	void vkDestroyDescriptorPool(Handle device, Handle descriptorPool, Handle pAllocator);

	/**
	 * Allocates a number of descriptor sets from a given pool.
	 * @param device				Logical device
	 * @param pAllocateInfo			Allocation descriptor
	 * @param pDescriptorSets		Returned descriptor set handles
	 * @return Result code
	 */
	int vkAllocateDescriptorSets(Handle device, VkDescriptorSetAllocateInfo pAllocateInfo, Pointer[] pDescriptorSets);

	/**
	 * Resets all descriptor sets in the given pool, i.e. recycles the resources back to the pool and releases the descriptor sets.
	 * @param device				Logical device
	 * @param descriptorPool		Descriptor set pool
	 * @param flags					Unused
	 * @return Result code
	 */
	int vkResetDescriptorPool(Handle device, Handle descriptorPool, int flags);

	/**
	 * Releases allocated descriptor sets.
	 * @param device				Logical device
	 * @param descriptorPool		Descriptor set pool
	 * @param descriptorSetCount	Number of descriptor sets
	 * @param pDescriptorSets		Descriptor set handles
	 * @return Result code
	 */
	int vkFreeDescriptorSets(Handle device, Handle descriptorPool, int descriptorSetCount, Handle pDescriptorSets);

	/**
	 * Updates the resources for one-or-more descriptor sets.
	 * @param device				Logical device
	 * @param descriptorWriteCount	Number of updates
	 * @param pDescriptorWrites		Update descriptors
	 * @param descriptorCopyCount	Number of copies
	 * @param pDescriptorCopies		Copy descriptors
	 */
	void vkUpdateDescriptorSets(Handle device, int descriptorWriteCount, VkWriteDescriptorSet[] pDescriptorWrites, int descriptorCopyCount, VkCopyDescriptorSet[] pDescriptorCopies);

	/**
	 * Binds one-or-more descriptor sets to the given pipeline.
	 * @param commandBuffer			Command buffer
	 * @param pipelineBindPoint		Bind point
	 * @param layout				Pipeline layout
	 * @param firstSet				Index of the first descriptor set
	 * @param descriptorSetCount	Number of descriptor sets
	 * @param pDescriptorSets		Descriptor sets to update
	 * @param dynamicOffsetCount	Number of dynamic offsets
	 * @param pDynamicOffsets		Dynamic offsets
	 */
	void vkCmdBindDescriptorSets(Handle commandBuffer, VkPipelineBindPoint pipelineBindPoint, Handle layout, int firstSet, int descriptorSetCount, Handle pDescriptorSets, int dynamicOffsetCount, int[] pDynamicOffsets);
}
