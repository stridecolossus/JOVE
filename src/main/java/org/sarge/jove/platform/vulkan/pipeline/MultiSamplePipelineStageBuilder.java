package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkSampleCount;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.util.IntegerArray;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.lib.util.Percentile;

/**
 * Builder for the multi-sample pipeline stage.
 * @see VkPipelineMultisampleStateCreateInfo
 * @author Sarge
 */
public class MultiSamplePipelineStageBuilder extends AbstractPipelineStageBuilder<VkPipelineMultisampleStateCreateInfo> {
	private VkPipelineMultisampleStateCreateInfo info = new VkPipelineMultisampleStateCreateInfo();

	public MultiSamplePipelineStageBuilder() {
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
	public MultiSamplePipelineStageBuilder rasterizationSamples(VkSampleCount rasterizationSamples) {
		info.rasterizationSamples = notNull(rasterizationSamples);
		return this;
	}

	/**
	 * Sets the number of rasterization samples.
	 * @param rasterizationSamples Sample count
	 * @throws IllegalArgumentException if {@link #samples} is not a valid {@link VkSampleCount}
	 * @see #samples(VkSampleCount)
	 */
	public MultiSamplePipelineStageBuilder samples(int rasterizationSamples) {
		info.rasterizationSamples = IntegerEnumeration.mapping(VkSampleCount.class).map(rasterizationSamples);
		return this;
	}

	/**
	 * Sets whether multi-sample shading is enabled (default is {@code false}).
	 * @param sampleShadingEnable Whether sample shading is enabled
	 */
	public MultiSamplePipelineStageBuilder sampleShadingEnable(boolean sampleShadingEnable) {
		info.sampleShadingEnable = VulkanBoolean.of(sampleShadingEnable);
		return this;
	}

	/**
	 * Sets the minimum fraction of sample shading (default is one).
	 * @param minSampleShading Minimum sample shading fraction
	 */
	public MultiSamplePipelineStageBuilder minSampleShading(Percentile minSampleShading) {
		info.minSampleShading = minSampleShading.floatValue();
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
	 * @param alphaToCoverageEnable Whether <i>alpha to coverage</i> is enabled
	 */
	public MultiSamplePipelineStageBuilder alphaToCoverageEnable(boolean alphaToCoverageEnable) {
		info.alphaToCoverageEnable = VulkanBoolean.of(alphaToCoverageEnable);
		return this;
	}

	/**
	 * Sets whether the alpha component of the first colour output is replaced with one.
	 * @param alphaToOneEnable Whether <i>alpha to one</i> is enabled
	 */
	public MultiSamplePipelineStageBuilder alphaToOneEnable(boolean alphaToOneEnable) {
		info.alphaToOneEnable = VulkanBoolean.of(alphaToOneEnable);
		return this;
	}

	@Override
	VkPipelineMultisampleStateCreateInfo get() {
		return info;
	}
}
