package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkSampleCount;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.util.IntegerArray;
import org.sarge.lib.util.Percentile;

import com.sun.jna.Pointer;

/**
 * Builder for the multi-sample pipeline stage.
 * @see VkPipelineMultisampleStateCreateInfo
 * @author Sarge
 */
public class MultiSamplePipelineStageBuilder extends AbstractPipelineStageBuilder<VkPipelineMultisampleStateCreateInfo, MultiSamplePipelineStageBuilder> {
	private VkSampleCount rasterizationSamples = VkSampleCount.COUNT_1;
	private boolean sampleShadingEnable;
	private float minSampleShading = 1;
	private boolean alphaToCoverageEnable;
	private boolean alphaToOneEnable;
	private Pointer mask;

	@Override
	void init(MultiSamplePipelineStageBuilder builder) {
		rasterizationSamples = builder.rasterizationSamples;
		sampleShadingEnable = builder.sampleShadingEnable;
		minSampleShading = builder.minSampleShading;
		alphaToCoverageEnable = builder.alphaToCoverageEnable;
		alphaToOneEnable = builder.alphaToOneEnable;
		mask = builder.mask;
	}

	/**
	 * Sets the number of rasterization samples.
	 * @param samples Sample count
	 * @see #samples(int)
	 */
	public MultiSamplePipelineStageBuilder rasterizationSamples(VkSampleCount rasterizationSamples) {
		this.rasterizationSamples = notNull(rasterizationSamples);
		return this;
	}

	/**
	 * Sets the number of rasterization samples.
	 * @param rasterizationSamples Sample count
	 * @throws IllegalArgumentException if the number of samples is not valid {@link VkSampleCount}
	 * @see #samples(VkSampleCount)
	 */
	public MultiSamplePipelineStageBuilder samples(int rasterizationSamples) {
		final VkSampleCount count = VkSampleCount.valueOf("COUNT_" + rasterizationSamples);
		return rasterizationSamples(count);
	}

	/**
	 * Sets whether multi-sample shading is enabled (default is {@code false}).
	 * @param sampleShadingEnable Whether sample shading is enabled
	 */
	public MultiSamplePipelineStageBuilder sampleShadingEnable(boolean sampleShadingEnable) {
		this.sampleShadingEnable = sampleShadingEnable;
		return this;
	}

	/**
	 * Sets the minimum fraction of sample shading (default is one).
	 * @param minSampleShading Minimum sample shading fraction
	 */
	public MultiSamplePipelineStageBuilder minSampleShading(Percentile minSampleShading) {
		this.minSampleShading = minSampleShading.floatValue();
		return this;
	}

	/**
	 * Sets the sample mask.
	 * @param mask Sample mask
	 */
	public MultiSamplePipelineStageBuilder sampleMask(int[] mask) {
		// TODO - length = samples / 32
		this.mask = new IntegerArray(mask);
		return this;
	}

	/**
	 * Sets whether an temporary coverage value is generated based on the alpha value of the first colour output.
	 * @param alphaToCoverageEnable Whether <i>alpha to coverage</i> is enabled
	 */
	public MultiSamplePipelineStageBuilder alphaToCoverageEnable(boolean alphaToCoverageEnable) {
		this.alphaToCoverageEnable = alphaToCoverageEnable;
		return this;
	}

	/**
	 * Sets whether the alpha component of the first colour output is replaced with one.
	 * @param alphaToOneEnable Whether <i>alpha to one</i> is enabled
	 */
	public MultiSamplePipelineStageBuilder alphaToOneEnable(boolean alphaToOneEnable) {
		this.alphaToOneEnable = alphaToOneEnable;
		return this;
	}

	@Override
	VkPipelineMultisampleStateCreateInfo get() {
		final var info = new VkPipelineMultisampleStateCreateInfo();
		info.rasterizationSamples = rasterizationSamples;
		info.sampleShadingEnable = VulkanBoolean.of(sampleShadingEnable);
		info.minSampleShading = minSampleShading;
		info.alphaToCoverageEnable = VulkanBoolean.of(alphaToCoverageEnable);
		info.alphaToOneEnable = VulkanBoolean.of(alphaToOneEnable);
		info.pSampleMask = mask;
		return info;
	}
}
