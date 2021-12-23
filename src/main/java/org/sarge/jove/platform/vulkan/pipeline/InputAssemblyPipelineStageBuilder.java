package org.sarge.jove.platform.vulkan.pipeline;

import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkPrimitiveTopology;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

/**
 * Builder for the input assembly pipeline stage.
 * @see VkPipelineInputAssemblyStateCreateInfo
 * @author Sarge
 */
public class InputAssemblyPipelineStageBuilder extends AbstractPipelineStageBuilder<VkPipelineInputAssemblyStateCreateInfo, InputAssemblyPipelineStageBuilder> {
	private VkPrimitiveTopology topology = VkPrimitiveTopology.TRIANGLE_STRIP;
	private boolean restart;

	@Override
	void init(InputAssemblyPipelineStageBuilder builder) {
		topology = builder.topology;
		restart = builder.restart;
	}

	/**
	 * Sets the primitive topology.
	 * @param primitive Primitive
	 */
	public InputAssemblyPipelineStageBuilder topology(Primitive primitive) {
		this.topology = primitive.topology();
		return this;
	}

	/**
	 * Sets whether primitive restart is enabled.
	 * @param restart Whether restart is enabled
	 */
	public InputAssemblyPipelineStageBuilder restart(boolean restart) {
		this.restart = restart;
		return this;
	}

	@Override
	VkPipelineInputAssemblyStateCreateInfo get() {
		final var info = new VkPipelineInputAssemblyStateCreateInfo();
		info.topology = topology;
		info.primitiveRestartEnable = VulkanBoolean.of(restart);
		return info;
	}
}
