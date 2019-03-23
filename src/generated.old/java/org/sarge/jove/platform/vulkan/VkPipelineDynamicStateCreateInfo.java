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
	"dynamicStateCount",
	"pDynamicStates"
})
public class VkPipelineDynamicStateCreateInfo extends Structure {
	public static class ByValue extends VkPipelineDynamicStateCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineDynamicStateCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int dynamicStateCount;
	public int pDynamicStates;
}
