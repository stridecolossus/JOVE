package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkCompareOp;
import org.sarge.jove.platform.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

public class DepthStencilStageBuilder extends AbstractPipelineBuilder<VkPipelineDepthStencilStateCreateInfo> {
	private final VkPipelineDepthStencilStateCreateInfo info = new VkPipelineDepthStencilStateCreateInfo();

	public DepthStencilStageBuilder() {
		enable(false);
		write(false);
		compare(VkCompareOp.VK_COMPARE_OP_LESS);

		// TODO
		info.depthBoundsTestEnable = VulkanBoolean.FALSE;
		info.minDepthBounds = 0;
		info.maxDepthBounds = 1;

		// TODO - stencil
		info.stencilTestEnable = VulkanBoolean.FALSE;
//		info.front = null;
//		info.back = null;
////		info.front = new VkStencilOpState();
////		info.back = new VkStencilOpState();
	}

	public DepthStencilStageBuilder enable(boolean enabled) {
		info.depthTestEnable = VulkanBoolean.of(enabled);
		return this;
	}

	public DepthStencilStageBuilder write(boolean write) {
		info.depthWriteEnable = VulkanBoolean.of(write);
		return this;
	}

	public DepthStencilStageBuilder compare(VkCompareOp op) {
		info.depthCompareOp = notNull(op);
		return this;
	}

	@Override
	protected VkPipelineDepthStencilStateCreateInfo result() {
		return info;
	}
}
