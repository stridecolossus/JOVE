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
	private boolean enable;
	private boolean write = true;
	private boolean bounds;
	private VkCompareOp op = VkCompareOp.LESS_OR_EQUAL;

	/**
	 * Sets whether depth-testing is enabled (default is {@code false}).
	 * @param enable Whether depth-test is enabled
	 */
	public DepthStencilStageBuilder enable(boolean enable) {
		this.enable = enable;
		return this;
	}

	/**
	 * Sets whether to write to the depth buffer (default is {@code true}).
	 * @param write Whether to write to the depth buffer
	 */
	public DepthStencilStageBuilder write(boolean write) {
		this.write = write;
		return this;
	}

	/**
	 * Sets the depth-test comparison function (default is {@link VkCompareOp#LESS}).
	 * @param op Depth-test function
	 */
	public DepthStencilStageBuilder compare(VkCompareOp op) {
		this.op = notNull(op);
		return this;
	}

	@Override
	VkPipelineDepthStencilStateCreateInfo get() {
		final var info = new VkPipelineDepthStencilStateCreateInfo();
		info.depthTestEnable = VulkanBoolean.of(enable);
		info.depthWriteEnable = VulkanBoolean.of(write);
		info.depthCompareOp = op;
		// TODO...
		info.depthBoundsTestEnable = VulkanBoolean.of(bounds);
		info.minDepthBounds = 0;
		info.maxDepthBounds = 1;
		info.stencilTestEnable = VulkanBoolean.FALSE;
		return info;
	}
}
