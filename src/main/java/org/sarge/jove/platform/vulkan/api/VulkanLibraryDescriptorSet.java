package org.sarge.jove.platform.vulkan.api;

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
	int vkCreateDescriptorSetLayout(Pointer device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSetLayout);
	void vkDestroyDescriptorSetLayout(Pointer device, Pointer descriptorSetLayout, Pointer pAllocator);

	int vkCreateDescriptorPool(Pointer device, VkDescriptorPoolCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pDescriptorPool);
	void vkDestroyDescriptorPool(Pointer device, Pointer descriptorPool, Pointer pAllocator);
	int vkResetDescriptorPool(Pointer device, Pointer descriptorPool, int flags);

	int vkAllocateDescriptorSets(Pointer device, VkDescriptorSetAllocateInfo pAllocateInfo, Pointer[] pDescriptorSets);
	int vkFreeDescriptorSets(Pointer device, Pointer descriptorPool, int descriptorSetCount, Pointer[] pDescriptorSets);

	void vkUpdateDescriptorSets(Pointer device, int descriptorWriteCount, VkWriteDescriptorSet[] pDescriptorWrites, int descriptorCopyCount, VkCopyDescriptorSet[] pDescriptorCopies);
	void vkCmdBindDescriptorSets(Pointer commandBuffer, VkPipelineBindPoint pipelineBindPoint, Pointer layout, int firstSet, int descriptorSetCount, Pointer[] pDescriptorSets, int dynamicOffsetCount, int[] pDynamicOffsets);
}