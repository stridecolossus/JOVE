package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkBufferCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.BUFFER_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public long size;
	public BitMask<VkBufferUsageFlag> usage;
	public VkSharingMode sharingMode;
	public int queueFamilyIndexCount;
	public int[] pQueueFamilyIndices;
}
