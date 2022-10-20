package org.sarge.jove.platform.vulkan.pipeline;

import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.RequiredFeature;

/**
 * Builder for the input assembly pipeline stage.
 * @author Sarge
 */
public class AssemblyPipelineStageBuilder extends AbstractPipelineStageBuilder<VkPipelineInputAssemblyStateCreateInfo> {
	private VkPipelineInputAssemblyStateCreateInfo info = new VkPipelineInputAssemblyStateCreateInfo();

	AssemblyPipelineStageBuilder() {
		topology(Primitive.TRIANGLE_STRIP);
		restart(false);
	}

	/**
	 * Sets the primitive topology.
	 * @param primitive Primitive
	 */
	public AssemblyPipelineStageBuilder topology(Primitive primitive) {
		info.topology = map(primitive);
		return this;
	}

	private static VkPrimitiveTopology map(Primitive primitive) {
		return switch(primitive) {
			case POINTS 		-> VkPrimitiveTopology.POINT_LIST;
			case LINES 			-> VkPrimitiveTopology.LINE_LIST;
			case LINE_STRIP 	-> VkPrimitiveTopology.LINE_STRIP;
			case TRIANGLES 		-> VkPrimitiveTopology.TRIANGLE_LIST;
			case TRIANGLE_STRIP	-> VkPrimitiveTopology.TRIANGLE_STRIP;
			case TRIANGLE_FAN	-> VkPrimitiveTopology.TRIANGLE_FAN;
			case PATCH			-> VkPrimitiveTopology.PATCH_LIST;
			default 			-> throw new RuntimeException();
		};
	}

	/**
	 * Sets whether primitive restart is enabled.
	 * @param restart Whether restart is enabled
	 */
	@RequiredFeature(field="primitiveRestartEnable", feature="primitiveTopologyListRestart")
	public AssemblyPipelineStageBuilder restart(boolean restart) {
		info.primitiveRestartEnable = restart;
		return this;
	}

	@Override
	VkPipelineInputAssemblyStateCreateInfo get() {
		return info;
	}
}
