package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.IntEnum.ReverseMapping;
import org.sarge.lib.Percentile;

/**
 * Builder for the multi-sample pipeline stage.
 * @see VkPipelineMultisampleStateCreateInfo
 * @author Sarge
 */
public class MultiSampleStage {
	private final VkPipelineMultisampleStateCreateInfo info = new VkPipelineMultisampleStateCreateInfo();

	public MultiSampleStage() {
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
	public MultiSampleStage rasterizationSamples(VkSampleCount rasterizationSamples) {
		info.rasterizationSamples = requireNonNull(rasterizationSamples);
		return this;
	}

	/**
	 * Sets the number of rasterization samples.
	 * @param rasterizationSamples Sample count
	 * @throws IllegalArgumentException if {@link #samples} is not a valid {@link VkSampleCount}
	 * @see #samples(VkSampleCount)
	 */
	public MultiSampleStage samples(int rasterizationSamples) {
		info.rasterizationSamples = ReverseMapping.mapping(VkSampleCount.class).map(rasterizationSamples);
		return this;
	}

	/**
	 * Sets whether multi-sample shading is enabled (default is {@code false}).
	 * @param sampleShadingEnable Whether sample shading is enabled
	 */
	public MultiSampleStage sampleShadingEnable(boolean sampleShadingEnable) {
		info.sampleShadingEnable = sampleShadingEnable;
		return this;
	}

	/**
	 * Sets the minimum fraction of sample shading (default is one).
	 * @param minSampleShading Minimum sample shading fraction
	 */
	public MultiSampleStage minSampleShading(Percentile minSampleShading) {
		info.minSampleShading = minSampleShading.value();
		return this;
	}

	/**
	 * Sets the sample mask.
	 * @param mask Sample mask
	 */
	public MultiSampleStage sampleMask(int[] mask) {
		// TODO - length = samples / 32
		info.pSampleMask = Arrays.copyOf(mask, mask.length);
		return this;
	}

	/**
	 * Sets whether an temporary coverage value is generated based on the alpha value of the first colour output.
	 * @param alphaToCoverageEnable Whether <i>alpha to coverage</i> is enabled
	 */
	public MultiSampleStage alphaToCoverageEnable(boolean alphaToCoverageEnable) {
		info.alphaToCoverageEnable = alphaToCoverageEnable;
		return this;
	}

	/**
	 * Sets whether the alpha component of the first colour output is replaced with one.
	 * @param alphaToOneEnable Whether <i>alpha to one</i> is enabled
	 */
	public MultiSampleStage alphaToOneEnable(boolean alphaToOneEnable) {
		info.alphaToOneEnable = alphaToOneEnable;
		return this;
	}

	/**
	 * @return Multi-sample descriptor
	 */
	VkPipelineMultisampleStateCreateInfo descriptor() {
		return info;
	}
}
