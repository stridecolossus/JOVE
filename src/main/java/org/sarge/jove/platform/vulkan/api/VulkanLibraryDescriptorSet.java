package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.VkCopyDescriptorSet;
import org.sarge.jove.platform.vulkan.VkDescriptorPoolCreateInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorSetAllocateInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkWriteDescriptorSet;
import org.sarge.jove.platform.vulkan.common.Command.Buffer;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.platform.vulkan.render.DescriptorSet.Layout;
import org.sarge.jove.platform.vulkan.render.DescriptorSet.Pool;

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
	int vkCreateDescriptorSetLayout(DeviceContext device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSetLayout);

	/**
	 * Destroys a descriptor set layout.
	 * @param device				Logical device
	 * @param descriptorSetLayout	Layout
	 * @param pAllocator			Allocator
	 */
	void vkDestroyDescriptorSetLayout(DeviceContext device, Layout descriptorSetLayout, Pointer pAllocator);

	/**
	 * Creates a descriptor-set pool.
	 * @param device				Logical device
	 * @param pCreateInfo			Descriptor
	 * @param pAllocator			Allocator
	 * @param pDescriptorPool		Returned pool
	 * @return Result code
	 */
	int vkCreateDescriptorPool(DeviceContext device, VkDescriptorPoolCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pDescriptorPool);

	/**
	 * Destroys a descriptor-set pool.
	 * @param device				Logical device
	 * @param descriptorPool		Pool
	 * @param pAllocator			Allocator
	 */
	void vkDestroyDescriptorPool(DeviceContext device, Pool descriptorPool, Pointer pAllocator);

	/**
	 * Allocates a number of descriptor sets from a given pool.
	 * @param device				Logical device
	 * @param pAllocateInfo			Allocation descriptor
	 * @param pDescriptorSets		Returned descriptor set handles
	 * @return Result code
	 */
	int vkAllocateDescriptorSets(DeviceContext device, VkDescriptorSetAllocateInfo pAllocateInfo, Pointer[] pDescriptorSets);

	/**
	 * Resets all descriptor sets in the given pool, i.e. recycles the resources back to the pool and releases the descriptor sets.
	 * @param device				Logical device
	 * @param descriptorPool		Descriptor set pool
	 * @param flags					Unused
	 * @return Result code
	 */
	int vkResetDescriptorPool(DeviceContext device, Pool descriptorPool, int flags);

	/**
	 * Releases allocated descriptor sets.
	 * @param device				Logical device
	 * @param descriptorPool		Descriptor set pool
	 * @param descriptorSetCount	Number of descriptor sets
	 * @param pDescriptorSets		Descriptor set handles
	 * @return Result code
	 */
	int vkFreeDescriptorSets(DeviceContext device, Pool descriptorPool, int descriptorSetCount, Pointer pDescriptorSets);

	/**
	 * Updates the resources for one-or-more descriptor sets.
	 * @param device				Logical device
	 * @param descriptorWriteCount	Number of updates
	 * @param pDescriptorWrites		Update descriptors
	 * @param descriptorCopyCount	Number of copies
	 * @param pDescriptorCopies		Copy descriptors
	 */
	void vkUpdateDescriptorSets(DeviceContext device, int descriptorWriteCount, VkWriteDescriptorSet[] pDescriptorWrites, int descriptorCopyCount, VkCopyDescriptorSet[] pDescriptorCopies);

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
	void vkCmdBindDescriptorSets(Buffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, PipelineLayout layout, int firstSet, int descriptorSetCount, Pointer pDescriptorSets, int dynamicOffsetCount, int[] pDynamicOffsets);
}
