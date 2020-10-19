package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkCompareOp;
import org.sarge.jove.platform.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

/**
 * Builder for the depth-stencil pipeline stage.
 * @author Sarge
 */
public class DepthStencilStageBuilder extends AbstractPipelineBuilder<VkPipelineDepthStencilStateCreateInfo> {
	private final VkPipelineDepthStencilStateCreateInfo info = new VkPipelineDepthStencilStateCreateInfo();

	/**
	 * Constructor.
	 */
	public DepthStencilStageBuilder() {
		enable(false);
		write(true);
		compare(VkCompareOp.VK_COMPARE_OP_LESS);

		// TODO
		info.depthBoundsTestEnable = VulkanBoolean.FALSE;
		info.minDepthBounds = 0;
		info.maxDepthBounds = 1;

		// TODO - stencil
		info.stencilTestEnable = VulkanBoolean.FALSE;
	}

	/**
	 * Sets whether depth-testing is enabled (default is {@code false}).
	 * @param enabled Whether depth-test is enabled
	 */
	public DepthStencilStageBuilder enable(boolean enabled) {
		info.depthTestEnable = VulkanBoolean.of(enabled);
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
	 * Sets the depth-test comparison function (default is {@link VkCompareOp#VK_COMPARE_OP_LESS}).
	 * @param op Depth-test function
	 */
	public DepthStencilStageBuilder compare(VkCompareOp op) {
		info.depthCompareOp = notNull(op);
		return this;
	}

	@Override
	protected VkPipelineDepthStencilStateCreateInfo result() {
		return info;
	}
}
