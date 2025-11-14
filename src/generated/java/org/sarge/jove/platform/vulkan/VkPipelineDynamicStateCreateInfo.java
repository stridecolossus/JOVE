package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineDynamicStateCreateInfo extends VulkanStructure {
	public final VkStructureType sType = VkStructureType.PIPELINE_DYNAMIC_STATE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public int dynamicStateCount;
	public int[] pDynamicStates;

	@Override
    public GroupLayout layout() {
    	return MemoryLayout.structLayout(
    			JAVA_INT.withName("sType"),
    			PADDING,
    			POINTER.withName("pNext"),
    			JAVA_INT.withName("flags"),
    			JAVA_INT.withName("dynamicStateCount"),
    			POINTER.withName("pDynamicStates")
    	);
    }
}
