package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkCompareOp;
import org.sarge.jove.platform.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

/**
 * Builder for the depth-stencil pipeline stage.
 * @see VkPipelineDepthStencilStateCreateInfo
 * @author Sarge
 */
public class DepthStencilStageBuilder extends AbstractPipelineBuilder<VkPipelineDepthStencilStateCreateInfo> {
	private final VkPipelineDepthStencilStateCreateInfo info = new VkPipelineDepthStencilStateCreateInfo();

	public DepthStencilStageBuilder() {
		enable(false);
		write(true);
		compare(VkCompareOp.LESS_OR_EQUAL);
		// TODO - other fields
		// depth bounds test
		// stencil test
		// front/back
		// min/max bounds
	}

	/**
	 * Sets whether depth-testing is enabled (default is {@code false}).
	 * @param enable Whether depth-test is enabled
	 */
	public DepthStencilStageBuilder enable(boolean enable) {
		info.depthTestEnable = VulkanBoolean.of(enable);
		return this;
	}

	/**
	 * Sets whether to write to the depth buffer (default is {@code true}).
	 * @param write Whether to write to the depth buffer
	 */
	public DepthStencilStageBuilder write(boolean write) {
		info.depthWriteEnable = VulkanBoolean.of(write);
		return this;
	}

	/**
	 * Sets the depth-test comparison function (default is {@link VkCompareOp#LESS}).
	 * @param op Depth-test function
	 */
	public DepthStencilStageBuilder compare(VkCompareOp op) {
		info.depthCompareOp = notNull(op);
		return this;
	}

	@Override
	VkPipelineDepthStencilStateCreateInfo get() {
		return info;
	}
}
