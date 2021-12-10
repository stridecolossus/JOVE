package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkSampleCount;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline.Builder;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.util.IntegerArray;
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
		alphaToCoverageEnable(false);
		alphaToOneEnable(false);
	}

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

	/**
	 * Sets the sample mask.
	 * @param mask Sample mask
	 */
	public MultiSamplePipelineStageBuilder sampleMask(int[] mask) {
		// TODO - length = samples / 32
		info.pSampleMask = new IntegerArray(mask);
		return this;
	}

	/**
	 * Sets whether an temporary coverage value is generated based on the alpha value of the first colour output.
	 * @param enable Whether <i>alpha to coverage</i> is enabled
	 */
	public MultiSamplePipelineStageBuilder alphaToCoverageEnable(boolean enable) {
		info.alphaToCoverageEnable = VulkanBoolean.of(enable);
		return this;
	}

	/**
	 * Sets whether the alpha component of the first colour output is replaced with one.
	 * @param enable Whether <i>alpha to one</i> is enabled
	 */
	public MultiSamplePipelineStageBuilder alphaToOneEnable(boolean enable) {
		info.alphaToOneEnable = VulkanBoolean.of(enable);
		return this;
	}

	@Override
	VkPipelineMultisampleStateCreateInfo get() {
		return info;
	}
}
