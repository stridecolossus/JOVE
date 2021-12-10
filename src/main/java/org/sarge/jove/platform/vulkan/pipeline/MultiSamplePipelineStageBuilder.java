package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkSampleCount;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline.Builder;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.lib.util.Percentile;

/**
 * Builder for the multi-sample pipeline stage.
 * @author Sarge
 */
public class MultiSamplePipelineStageBuilder extends AbstractPipelineStageBuilder<VkPipelineMultisampleStateCreateInfo> {
	private final VkPipelineMultisampleStateCreateInfo info = new VkPipelineMultisampleStateCreateInfo();

	MultiSamplePipelineStageBuilder(Builder parent) {
		super(parent);
		samples(VkSampleCount.COUNT_1);
		sampleShadingEnable(false);
		minSampleShading(Percentile.ONE);
	}
	// TODO - others
	// - pSampleMask - pointer to int array, samples / 32
	// - alphaToCoverageEnable
	// - alphaToOneEnable

	/**
	 * Sets the number of rasterization samples.
	 * @param samples Sample count
	 * @see #samples(int)
	 */
	public MultiSamplePipelineStageBuilder samples(VkSampleCount samples) {
		info.rasterizationSamples = notNull(samples);
		return this;
	}

	/**
	 * Sets the number of rasterization samples.
	 * @param samples Sample count
	 * @throws IllegalArgumentException if the number of samples is not valid {@link VkSampleCount}
	 * @see #samples(VkSampleCount)
	 */
	public MultiSamplePipelineStageBuilder samples(int samples) {
		final VkSampleCount count = VkSampleCount.valueOf("COUNT_" + samples);
		return samples(count);
	}

	/**
	 * Sets whether multi-sample shading is enabled (default is {@code false}).
	 * @param enable Whether sample shading is enabled
	 */
	public MultiSamplePipelineStageBuilder sampleShadingEnable(boolean enable) {
		info.sampleShadingEnable = VulkanBoolean.of(enable);
		return this;
	}

	/**
	 * Sets the minimum fraction of sample shading (default is one).
	 * @param min Minimum sample shading fraction
	 */
	public MultiSamplePipelineStageBuilder minSampleShading(Percentile min) {
		info.minSampleShading = min.floatValue();
		return this;
	}

	@Override
	VkPipelineMultisampleStateCreateInfo get() {
		return info;
	}
}
