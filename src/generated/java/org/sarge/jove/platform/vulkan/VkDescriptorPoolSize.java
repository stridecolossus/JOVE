package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkDescriptorPoolSize implements NativeStructure {
	public VkDescriptorType type;
	public int descriptorCount;

	@Override
    public GroupLayout layout() {
    	return MemoryLayout.structLayout(
    			JAVA_INT.withName("type"),
    			JAVA_INT.withName("descriptorCount")
    	);
    }
}
