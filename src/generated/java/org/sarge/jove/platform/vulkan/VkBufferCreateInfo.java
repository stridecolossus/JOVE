package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkBufferCreateInfo extends VulkanStructure {
	public final VkStructureType sType = VkStructureType.BUFFER_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public long size;
	public EnumMask<VkBufferUsageFlag> usage;
	public VkSharingMode sharingMode;
	public int queueFamilyIndexCount;
	public int[] pQueueFamilyIndices;
}
