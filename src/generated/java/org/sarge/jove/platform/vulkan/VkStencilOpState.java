package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkStencilOpState implements NativeStructure {
	public VkStencilOp failOp;
	public VkStencilOp passOp;
	public VkStencilOp depthFailOp;
	public VkCompareOp compareOp;
	public int compareMask;
	public int writeMask;
	public int reference;

	@Override
    public GroupLayout layout() {
    	return MemoryLayout.structLayout(
    			JAVA_INT.withName("failOp"),
    			JAVA_INT.withName("passOp"),
    			JAVA_INT.withName("depthFailOp"),
    			JAVA_INT.withName("compareOp"),
    			JAVA_INT.withName("compareMask"),
    			JAVA_INT.withName("writeMask"),
    			JAVA_INT.withName("reference")
    	);
    }
}
