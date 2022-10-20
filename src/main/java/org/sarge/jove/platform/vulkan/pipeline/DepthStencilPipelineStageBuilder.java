package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.RequiredFeature;

/**
 * Builder for the depth-stencil pipeline stage.
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

	/**
	 * Sets whether depth-testing is enabled (default is {@code false}).
	 * @param depthTestEnable Whether depth-test is enabled
	 */
	@RequiredFeature(field="depthTestEnable", feature="depthBounds")
	public DepthStencilPipelineStageBuilder enable(boolean depthTestEnable) {
		info.depthTestEnable = depthTestEnable;
		return this;
	}

	/**
	 * Sets whether to write to the depth buffer (default is {@code true}).
	 * @param depthWriteEnable Whether to write to the depth buffer
	 */
	public DepthStencilPipelineStageBuilder write(boolean depthWriteEnable) {
		info.depthWriteEnable = depthWriteEnable;
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
