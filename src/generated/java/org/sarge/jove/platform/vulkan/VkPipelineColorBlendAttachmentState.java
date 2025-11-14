package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineColorBlendAttachmentState implements NativeStructure {
	public boolean blendEnable;
	public VkBlendFactor srcColorBlendFactor;
	public VkBlendFactor dstColorBlendFactor;
	public VkBlendOp colorBlendOp;
	public VkBlendFactor srcAlphaBlendFactor;
	public VkBlendFactor dstAlphaBlendFactor;
	public VkBlendOp alphaBlendOp;
	public EnumMask<VkColorComponent> colorWriteMask;

	@Override
    public GroupLayout layout() {
    	return MemoryLayout.structLayout(
    			JAVA_INT.withName("blendEnable"),
    			JAVA_INT.withName("srcColorBlendFactor"),
    			JAVA_INT.withName("dstColorBlendFactor"),
    			JAVA_INT.withName("colorBlendOp"),
    			JAVA_INT.withName("srcAlphaBlendFactor"),
    			JAVA_INT.withName("dstAlphaBlendFactor"),
    			JAVA_INT.withName("alphaBlendOp"),
    			JAVA_INT.withName("colorWriteMask")
    	);
    }
}
