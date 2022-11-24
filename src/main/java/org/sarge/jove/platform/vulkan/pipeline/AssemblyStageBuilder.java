package org.sarge.jove.platform.vulkan.pipeline;

import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.RequiredFeature;

/**
 * Builder for the input assembly pipeline stage.
 * @author Sarge
 */
public class AssemblyStageBuilder extends AbstractStageBuilder<VkPipelineInputAssemblyStateCreateInfo> {
	private VkPrimitiveTopology topology;
	private boolean restart;

	/**
	 * Constructor.
	 */
	AssemblyStageBuilder() {
		topology(Primitive.TRIANGLE_STRIP);
	}

	/**
	 * Sets the primitive topology.
	 * @param primitive Primitive
	 */
	public AssemblyStageBuilder topology(Primitive primitive) {
		this.topology = switch(primitive) {
			case POINT 			-> VkPrimitiveTopology.POINT_LIST;
			case LINE 			-> VkPrimitiveTopology.LINE_LIST;
			case LINE_STRIP 	-> VkPrimitiveTopology.LINE_STRIP;
			case TRIANGLE 		-> VkPrimitiveTopology.TRIANGLE_LIST;
			case TRIANGLE_STRIP	-> VkPrimitiveTopology.TRIANGLE_STRIP;
			case TRIANGLE_FAN	-> VkPrimitiveTopology.TRIANGLE_FAN;
			case PATCH			-> VkPrimitiveTopology.PATCH_LIST;
			default 			-> throw new RuntimeException();
		};
		return this;
	}

	/**
	 * Sets whether primitive restart is enabled.
	 * @param restart Whether restart is enabled
	 */
	@RequiredFeature(field="primitiveRestartEnable", feature="primitiveTopologyListRestart")
	public AssemblyStageBuilder restart(boolean restart) {
		this.restart = restart;
		return this;
	}

	@Override
	VkPipelineInputAssemblyStateCreateInfo get() {
		final var info = new VkPipelineInputAssemblyStateCreateInfo();
		info.topology = topology;
		info.primitiveRestartEnable = restart;
		return info;
	}
}
