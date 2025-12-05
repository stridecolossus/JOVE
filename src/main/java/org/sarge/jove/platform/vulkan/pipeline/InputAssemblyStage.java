package org.sarge.jove.platform.vulkan.pipeline;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.vulkan.*;

/**
 * Input assembly pipeline stage.
 * @see <a href="https://registry.khronos.org/vulkan/specs/1.1/html/chap20.html#drawing-primitive-topologies">Vulkan primitive topologies</a>
 * @author Sarge
 */
public class InputAssemblyStage {
	private VkPrimitiveTopology topology = VkPrimitiveTopology.TRIANGLE_STRIP;
	private boolean restart;

	/**
	 * Sets the primitive topology.
	 * @param primitive Drawing primitive
	 */
	public InputAssemblyStage topology(Primitive primitive) {
		topology = switch(primitive) {
			case POINT 			-> VkPrimitiveTopology.POINT_LIST;
			case LINE 			-> VkPrimitiveTopology.LINE_LIST;
			case LINE_STRIP 	-> VkPrimitiveTopology.LINE_STRIP;
			case TRIANGLE 		-> VkPrimitiveTopology.TRIANGLE_LIST;
			case TRIANGLE_STRIP	-> VkPrimitiveTopology.TRIANGLE_STRIP;
			case TRIANGLE_FAN	-> VkPrimitiveTopology.TRIANGLE_FAN;
			case PATCH			-> VkPrimitiveTopology.PATCH_LIST;
		};
		return this;
	}

	/**
	 * @param restart Whether restart is enabled
	 */
	public InputAssemblyStage restart(boolean restart) {
		this.restart = restart;
		return this;
	}

	/**
	 * @return Input assembly descriptor
	 */
	public VkPipelineInputAssemblyStateCreateInfo descriptor() {
		final var info = new VkPipelineInputAssemblyStateCreateInfo();
		info.sType = VkStructureType.PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
		info.topology = topology;
		info.primitiveRestartEnable = restart;
		return info;
	}
}
