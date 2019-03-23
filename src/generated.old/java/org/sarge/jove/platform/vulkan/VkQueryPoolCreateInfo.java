package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

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
public class VkQueryPoolCreateInfo extends Structure {
	public static class ByValue extends VkQueryPoolCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkQueryPoolCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_QUERY_POOL_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int queryType;
	public int queryCount;
	public int pipelineStatistics;
}
