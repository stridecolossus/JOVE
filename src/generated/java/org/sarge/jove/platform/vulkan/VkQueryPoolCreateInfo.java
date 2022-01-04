package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"queryType",
	"queryCount",
	"pipelineStatistics"
})
public class VkQueryPoolCreateInfo extends VulkanStructure {
	public static class ByValue extends VkQueryPoolCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkQueryPoolCreateInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.QUERY_POOL_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public VkQueryType queryType;
	public int queryCount;
	public VkQueryPipelineStatisticFlag pipelineStatistics;
}
