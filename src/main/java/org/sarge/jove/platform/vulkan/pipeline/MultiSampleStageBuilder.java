package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.*;
import org.sarge.lib.Percentile;

/**
 * Builder for the multi-sample pipeline stage.
 * @see VkPipelineMultisampleStateCreateInfo
 * @author Sarge
 */
public class MultiSampleStageBuilder extends AbstractStageBuilder<VkPipelineMultisampleStateCreateInfo> {
	private VkPipelineMultisampleStateCreateInfo info = new VkPipelineMultisampleStateCreateInfo();

	public MultiSampleStageBuilder() {
		samples(1);
		sampleShadingEnable(false);
		minSampleShading(Percentile.ONE);
		alphaToCoverageEnable(false);
		alphaToOneEnable(false);
	}

	/**
	 * Sets the number of rasterization samples.
	 * @param samples Sample count
	 * @see #samples(int)
	 */
	public MultiSampleStageBuilder rasterizationSamples(VkSampleCount rasterizationSamples) {
		info.rasterizationSamples = requireNonNull(rasterizationSamples);
		return this;
	}

	/**
	 * Sets the number of rasterization samples.
	 * @param rasterizationSamples Sample count
	 * @throws IllegalArgumentException if {@link #samples} is not a valid {@link VkSampleCount}
	 * @see #samples(VkSampleCount)
	 */
	public MultiSampleStageBuilder samples(int rasterizationSamples) {
		info.rasterizationSamples = IntEnum.reverse(VkSampleCount.class).map(rasterizationSamples);
		return this;
	}

	/**
	 * Sets whether multi-sample shading is enabled (default is {@code false}).
	 * @param sampleShadingEnable Whether sample shading is enabled
	 */
	public MultiSampleStageBuilder sampleShadingEnable(boolean sampleShadingEnable) {
		info.sampleShadingEnable = sampleShadingEnable;
		return this;
	}

	/**
	 * Sets the minimum fraction of sample shading (default is one).
	 * @param minSampleShading Minimum sample shading fraction
	 */
	public MultiSampleStageBuilder minSampleShading(Percentile minSampleShading) {
		info.minSampleShading = minSampleShading.floatValue();
		return this;
	}

	/**
	 * Sets the sample mask.
	 * @param mask Sample mask
	 */
	public MultiSampleStageBuilder sampleMask(int[] mask) {
		// TODO - length = samples / 32
		info.pSampleMask = new PointerToIntArray(mask);
		return this;
	}

	/**
	 * Sets whether an temporary coverage value is generated based on the alpha value of the first colour output.
	 * @param alphaToCoverageEnable Whether <i>alpha to coverage</i> is enabled
	 */
	public MultiSampleStageBuilder alphaToCoverageEnable(boolean alphaToCoverageEnable) {
		info.alphaToCoverageEnable = alphaToCoverageEnable;
		return this;
	}

	/**
	 * Sets whether the alpha component of the first colour output is replaced with one.
	 * @param alphaToOneEnable Whether <i>alpha to one</i> is enabled
	 */
	public MultiSampleStageBuilder alphaToOneEnable(boolean alphaToOneEnable) {
		info.alphaToOneEnable = alphaToOneEnable;
		return this;
	}

	@Override
	VkPipelineMultisampleStateCreateInfo get() {
		return info;
	}
}
