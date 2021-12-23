package org.sarge.jove.platform.vulkan.pipeline;

import org.sarge.jove.platform.vulkan.VkPipelineTessellationStateCreateInfo;

/**
 * Builder for the tesselation control pipeline stage.
 * @see VkPipelineTessellationStateCreateInfo
 * @author Sarge
 */
public class TesselationPipelineStageBuilder extends AbstractPipelineStageBuilder<VkPipelineTessellationStateCreateInfo, TesselationPipelineStageBuilder> {
	private int points;

	@Override
	void init(TesselationPipelineStageBuilder builder) {
		points = builder.points;
	}

	/**
	 * Sets the number of patch control points.
	 * @param points Number of control points
	 */
	public TesselationPipelineStageBuilder points(int points) {
		this.points = points;
		return this;
	}

	@Override
	VkPipelineTessellationStateCreateInfo get() {
		if(points == 0) {
			return null;
		}

		final var info = new VkPipelineTessellationStateCreateInfo();
		info.patchControlPoints = points;
		return info;
	}
}
