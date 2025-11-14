package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineRasterizationStateCreateInfo extends VulkanStructure {
	public final VkStructureType sType = VkStructureType.PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public boolean depthClampEnable;
	public boolean rasterizerDiscardEnable;
	public VkPolygonMode polygonMode;
	public VkCullMode cullMode;
	public VkFrontFace frontFace;
	public boolean depthBiasEnable;
	public float depthBiasConstantFactor;
	public float depthBiasClamp;
	public float depthBiasSlopeFactor;
	public float lineWidth;

    @Override
    public GroupLayout layout() {
    	return MemoryLayout.structLayout(
    			JAVA_INT.withName("sType"),
    			PADDING,
    			POINTER.withName("pNext"),
    			JAVA_INT.withName("flags"),
    			JAVA_INT.withName("depthClampEnable"),
    			JAVA_INT.withName("rasterizerDiscardEnable"),
    			JAVA_INT.withName("polygonMode"),
    			JAVA_INT.withName("cullMode"),
    			JAVA_INT.withName("frontFace"),
    			JAVA_INT.withName("depthBiasEnable"),
    			JAVA_FLOAT.withName("depthBiasConstantFactor"),
    			JAVA_FLOAT.withName("depthBiasClamp"),
    			JAVA_FLOAT.withName("depthBiasSlopeFactor"),
    			JAVA_FLOAT.withName("lineWidth"),
    			PADDING
    	);
    }
}
