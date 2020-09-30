package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkPrimitiveTopology;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

/**
 * Builder for the input assembly pipeline stage.
 * @author Sarge
 */
public class InputAssemblyStageBuilder extends AbstractPipelineStageBuilder<VkPipelineInputAssemblyStateCreateInfo> {
	private VkPrimitiveTopology topology = VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
	private boolean restart;

	/**
	 * Sets the primitive topology.
	 * @param topology Primitive topology
	 */
	public InputAssemblyStageBuilder topology(VkPrimitiveTopology topology) {
		this.topology = notNull(topology);
		return this;
	}

	/**
	 * Sets whether primitive restart is enabled.
	 * @param restart Whether restart is enabled
	 */
	public InputAssemblyStageBuilder restart(boolean restart) {
		this.restart = restart;
		return this;
	}

	@Override
	protected VkPipelineInputAssemblyStateCreateInfo result() {
		final var info = new VkPipelineInputAssemblyStateCreateInfo();
		info.topology = topology;
		info.primitiveRestartEnable = VulkanBoolean.of(restart);
		return info;
	}
}
