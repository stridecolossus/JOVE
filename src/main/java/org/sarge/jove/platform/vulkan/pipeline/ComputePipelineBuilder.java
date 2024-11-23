package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.util.BitMask;

/**
 * Builder for a compute pipeline.
 * @author Sarge
 */
public class ComputePipelineBuilder implements DelegatePipelineBuilder<VkComputePipelineCreateInfo> {
	private final ProgrammableShaderStage shader;

	/**
	 * Constructor.
	 * @param shader Compute shader
	 */
	protected ComputePipelineBuilder(ProgrammableShaderStage shader) {
		this.shader = requireNonNull(shader);
	}

	@Override
	public VkPipelineBindPoint type() {
		return VkPipelineBindPoint.COMPUTE;
	}

	@Override
	public VkComputePipelineCreateInfo identity() {
		return new VkComputePipelineCreateInfo();
	}

	@Override
	public void populate(BitMask<VkPipelineCreateFlag> flags, PipelineLayout layout, Handle base, int parent, VkComputePipelineCreateInfo info) {
		// Init descriptor
		info.flags = flags;
		info.layout = layout.handle();

		// Init derivative pipelines
		info.basePipelineHandle = base;
		info.basePipelineIndex = parent;

		// Add compute shader
		info.stage = new VkPipelineShaderStageCreateInfo();
		shader.populate(info.stage);
	}

	@Override
	public int create(DeviceContext dev, PipelineCache cache, VkComputePipelineCreateInfo[] array, Handle[] handles) {
		return dev.vulkan().library().vkCreateComputePipelines(dev, cache, array.length, array, null, handles);
	}
}
