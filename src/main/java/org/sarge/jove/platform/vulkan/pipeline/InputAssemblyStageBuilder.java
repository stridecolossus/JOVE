package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.util.Check.notNull;

import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkPrimitiveTopology;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

/**
 * Builder for the input assembly pipeline stage.
 * @author Sarge
 */
public class InputAssemblyStageBuilder extends AbstractPipelineBuilder<VkPipelineInputAssemblyStateCreateInfo> {
	private VkPrimitiveTopology topology = VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP;
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
	 * Sets the primitive topology.
	 * @param primitive JOVE primitive
	 */
	public InputAssemblyStageBuilder topology(Primitive primitive) {
		return topology(map(primitive));
	}

	private static VkPrimitiveTopology map(Primitive primitive) {
		return switch(primitive) {
			case LINES -> VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_LINE_LIST;
			case LINE_STRIP -> VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP;
			case POINTS -> VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_POINT_LIST;
			case TRIANGLE_FAN -> VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_FAN;
			case TRIANGLES-> VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
			case TRIANGLE_STRIP -> VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP;
			default -> throw new UnsupportedOperationException("Unsupported drawing primitive: " + primitive);
		};
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
