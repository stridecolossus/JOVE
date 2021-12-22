package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkCompareOp;
import org.sarge.jove.platform.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline.Builder;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

/**
 * Builder for the depth-stencil pipeline stage.
 * @see VkPipelineDepthStencilStateCreateInfo
 * @author Sarge
 */
public class DepthStencilPipelineStageBuilder extends AbstractPipelineStageBuilder<VkPipelineDepthStencilStateCreateInfo> {
	private boolean depthTestEnable;
	private boolean depthWriteEnable = true;
	private VkCompareOp depthCompareOp = VkCompareOp.LESS_OR_EQUAL;
	// TODO - other fields
	// depth bounds test
	// stencil test
	// front/back
	// min/max bounds

	DepthStencilPipelineStageBuilder(Builder parent) {
		super(parent);
	}

	/**
	 * Sets whether depth-testing is enabled (default is {@code false}).
	 * @param depthTestEnable Whether depth-test is enabled
	 */
	public DepthStencilPipelineStageBuilder enable(boolean depthTestEnable) {
		this.depthTestEnable = depthTestEnable;
		return this;
	}

	/**
	 * Sets whether to write to the depth buffer (default is {@code true}).
	 * @param depthWriteEnable Whether to write to the depth buffer
	 */
	public DepthStencilPipelineStageBuilder write(boolean depthWriteEnable) {
		this.depthWriteEnable = depthWriteEnable;
		return this;
	}

	/**
	 * Sets the depth-test comparison function (default is {@link VkCompareOp#LESS}).
	 * @param depthCompareOp Comparison function
	 */
	public DepthStencilPipelineStageBuilder compare(VkCompareOp depthCompareOp) {
		this.depthCompareOp = notNull(depthCompareOp);
		return this;
	}

	@Override
	VkPipelineDepthStencilStateCreateInfo get() {
		final VkPipelineDepthStencilStateCreateInfo info = new VkPipelineDepthStencilStateCreateInfo();
		info.depthTestEnable = VulkanBoolean.of(depthTestEnable);
		info.depthWriteEnable = VulkanBoolean.of(depthWriteEnable);
		info.depthCompareOp = depthCompareOp;
		return info;
	}
}
