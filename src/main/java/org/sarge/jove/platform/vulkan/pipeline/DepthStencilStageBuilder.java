package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.RequiredFeature;

/**
 * Builder for the depth-stencil pipeline stage.
 * @author Sarge
 */
public class DepthStencilStageBuilder extends AbstractStageBuilder<VkPipelineDepthStencilStateCreateInfo> {
	private final VkPipelineDepthStencilStateCreateInfo info = new VkPipelineDepthStencilStateCreateInfo();

	DepthStencilStageBuilder() {
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
	public DepthStencilStageBuilder enable(boolean depthTestEnable) {
		info.depthTestEnable = depthTestEnable;
		return this;
	}

	/**
	 * Sets whether to write to the depth buffer (default is {@code true}).
	 * @param depthWriteEnable Whether to write to the depth buffer
	 */
	public DepthStencilStageBuilder write(boolean depthWriteEnable) {
		info.depthWriteEnable = depthWriteEnable;
		return this;
	}

	/**
	 * Sets the depth-test comparison function (default is {@link VkCompareOp#LESS}).
	 * @param depthCompareOp Comparison function
	 */
	public DepthStencilStageBuilder compare(VkCompareOp depthCompareOp) {
		info.depthCompareOp = notNull(depthCompareOp);
		return this;
	}

	@Override
	VkPipelineDepthStencilStateCreateInfo get() {
		return info;
	}
}
