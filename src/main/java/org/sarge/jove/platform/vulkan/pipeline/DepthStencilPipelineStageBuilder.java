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
public class DepthStencilPipelineStageBuilder extends AbstractPipelineStageBuilder<VkPipelineDepthStencilStateCreateInfo> {
	private VkPipelineDepthStencilStateCreateInfo info = new VkPipelineDepthStencilStateCreateInfo();

	DepthStencilPipelineStageBuilder() {
		enable(false);
		write(true);
		compare(VkCompareOp.LESS_OR_EQUAL);
		// TODO - other fields
		// depth bounds test
		// stencil test
		// front/back
		// min/max bounds
	}

	void copy(DepthStencilPipelineStageBuilder builder) {
		this.info = builder.info.copy();
	}

	/**
	 * Sets whether depth-testing is enabled (default is {@code false}).
	 * @param depthTestEnable Whether depth-test is enabled
	 */
	public DepthStencilPipelineStageBuilder enable(boolean depthTestEnable) {
		info.depthTestEnable = VulkanBoolean.of(depthTestEnable);
		return this;
	}

	/**
	 * Sets whether to write to the depth buffer (default is {@code true}).
	 * @param depthWriteEnable Whether to write to the depth buffer
	 */
	public DepthStencilPipelineStageBuilder write(boolean depthWriteEnable) {
		info.depthWriteEnable = VulkanBoolean.of(depthWriteEnable);
		return this;
	}

	/**
	 * Sets the depth-test comparison function (default is {@link VkCompareOp#LESS}).
	 * @param depthCompareOp Comparison function
	 */
	public DepthStencilPipelineStageBuilder compare(VkCompareOp depthCompareOp) {
		info.depthCompareOp = notNull(depthCompareOp);
		return this;
	}

	@Override
	VkPipelineDepthStencilStateCreateInfo get() {
		return info;
	}
}
