package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineCacheCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.PIPELINE_CACHE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public long initialDataSize;
	public byte[] pInitialData;
}
