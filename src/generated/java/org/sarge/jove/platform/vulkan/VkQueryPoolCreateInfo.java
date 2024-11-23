package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkQueryPoolCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.QUERY_POOL_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public VkQueryType queryType;
	public int queryCount;
	public BitMask<VkQueryPipelineStatisticFlag> pipelineStatistics;
}
