package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.Validation.requireOneOrMore;

import org.sarge.jove.platform.vulkan.VkPipelineTessellationStateCreateInfo;

/**
 * Builder for the tesselation control pipeline stage.
 * @see VkPipelineTessellationStateCreateInfo
 * @author Sarge
 */
public class TesselationStage {
	private int points;

	/**
	 * Sets the number of patch control points.
	 * @param points Number of control points
	 */
	public TesselationStage points(int points) {
		this.points = requireOneOrMore(points);
		return this;
	}

	VkPipelineTessellationStateCreateInfo descriptor() {
		if(points == 0) {
			return null;
		}

		final var info = new VkPipelineTessellationStateCreateInfo();
		info.patchControlPoints = points;
		return info;
	}
}

// TODO - maxTessellationPatchSize
//public int maxTessellationGenerationLevel;
//public int maxTessellationPatchSize;
//public int maxTessellationControlPerVertexInputComponents;
//public int maxTessellationControlPerVertexOutputComponents;
//public int maxTessellationControlPerPatchOutputComponents;
//public int maxTessellationControlTotalOutputComponents;
//public int maxTessellationEvaluationInputComponents;
//public int maxTessellationEvaluationOutputComponents;
