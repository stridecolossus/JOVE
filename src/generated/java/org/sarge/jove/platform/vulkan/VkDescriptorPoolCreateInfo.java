package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkDescriptorPoolCreateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.DESCRIPTOR_POOL_CREATE_INFO;
	public Handle pNext;
	public EnumMask<VkDescriptorPoolCreateFlag> flags;
	public int maxSets;
	public int poolSizeCount;
	public VkDescriptorPoolSize[] pPoolSizes;

	@Override
    public GroupLayout layout() {
    	return MemoryLayout.structLayout(
    			JAVA_INT.withName("sType"),
    			PADDING,
    			POINTER.withName("pNext"),
    			JAVA_INT.withName("flags"),
    			JAVA_INT.withName("maxSets"),
    			JAVA_INT.withName("poolSizeCount"),
    			PADDING,
    			POINTER.withName("pPoolSizes")
    	);
    }
}
