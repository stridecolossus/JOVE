package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkDescriptorSetAllocateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.DESCRIPTOR_SET_ALLOCATE_INFO;
	public Handle pNext;
	public Handle descriptorPool;
	public int descriptorSetCount;
	public Handle[] pSetLayouts;

	@Override
    public GroupLayout layout() {
    	return MemoryLayout.structLayout(
    			JAVA_INT.withName("sType"),
    			PADDING,
    			POINTER.withName("pNext"),
    			POINTER.withName("descriptorPool"),
    			JAVA_INT.withName("descriptorSetCount"),
    			PADDING,
    			POINTER.withName("pSetLayouts")
    	);
    }
}
