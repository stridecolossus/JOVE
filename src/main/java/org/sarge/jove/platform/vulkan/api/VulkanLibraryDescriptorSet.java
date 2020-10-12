package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkCopyDescriptorSet;
import org.sarge.jove.platform.vulkan.VkDescriptorPoolCreateInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorSetAllocateInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Descriptor sets API.
 */
interface VulkanLibraryDescriptorSet {
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

	// TODO
	int vkResetDescriptorPool(Handle device, Handle descriptorPool, int flags);

	int vkAllocateDescriptorSets(Handle device, VkDescriptorSetAllocateInfo pAllocateInfo, Pointer[] pDescriptorSets);

	int vkFreeDescriptorSets(Handle device, Handle descriptorPool, int descriptorSetCount, Pointer[] pDescriptorSets);

	////

	int vkCreateDescriptorSetLayout(Handle device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Handle pAllocator, PointerByReference pSetLayout);
	void vkDestroyDescriptorSetLayout(Handle device, Handle descriptorSetLayout, Handle pAllocator);


	void vkUpdateDescriptorSets(Handle device, int descriptorWriteCount, /*VkWriteDescriptorSet[]*/ Pointer pDescriptorWrites, int descriptorCopyCount, VkCopyDescriptorSet[] pDescriptorCopies);

	void vkCmdBindDescriptorSets(Handle commandBuffer, VkPipelineBindPoint pipelineBindPoint, Handle layout, int firstSet, int descriptorSetCount, Pointer[] pDescriptorSets, int dynamicOffsetCount, int[] pDynamicOffsets);
}
