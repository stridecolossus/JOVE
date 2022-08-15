package org.sarge.jove.platform.vulkan.pipeline;

import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.sarge.jove.platform.vulkan.util.*;

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
		info.topology = primitive.topology();
		return this;
	}

	/**
	 * Sets whether primitive restart is enabled.
	 * @param restart Whether restart is enabled
	 */
	@RequiredFeature(field="primitiveRestartEnable", feature="primitiveTopologyListRestart")
	public AssemblyPipelineStageBuilder restart(boolean restart) {
		info.primitiveRestartEnable = VulkanBoolean.of(restart);
		return this;
	}

	@Override
	VkPipelineInputAssemblyStateCreateInfo get() {
		return info;
	}
}
