package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkDescriptorImageInfo implements NativeStructure {
	public Handle sampler;
	public Handle imageView;
	public VkImageLayout imageLayout;

	@Override
	public GroupLayout layout() {
	    return MemoryLayout.structLayout(
	            POINTER.withName("sampler"),
	            POINTER.withName("imageView"),
	            JAVA_INT.withName("imageLayout"),
	            PADDING
	    );
	}
}
