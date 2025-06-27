package org.sarge.jove.platform.vulkan.pipeline;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.RequiredFeature;

/**
 * Input assembly pipeline stage.
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
		this.topology = primitive.topology();
		return this;
	}

	/**
	 * Sets whether primitive restart is enabled.
	 * @param restart Whether restart is enabled
	 */
	@RequiredFeature(field="primitiveRestartEnable", feature="primitiveTopologyListRestart")
	public InputAssemblyStage restart(boolean restart) {
		this.restart = restart;
		return this;
	}

	/**
	 * @return Input assembly descriptor
	 */
	public VkPipelineInputAssemblyStateCreateInfo descriptor() {
		final var info = new VkPipelineInputAssemblyStateCreateInfo();
		info.topology = topology;
		info.primitiveRestartEnable = restart;
		return info;
	}
}
