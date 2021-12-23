package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkSampleCount;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.util.IntegerArray;
import org.sarge.lib.util.Percentile;

public class MultiSamplePipelineStageBuilderTest {
	private MultiSamplePipelineStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new MultiSamplePipelineStageBuilder();
	}

	@Test
	void build() {
		final VkPipelineMultisampleStateCreateInfo info = builder
				.samples(8)
				.sampleShadingEnable(true)
				.minSampleShading(Percentile.HALF)
				.sampleMask(new int[1])
				.alphaToCoverageEnable(true)
				.alphaToOneEnable(true)
				.get();

		assertNotNull(info);
		assertEquals(0, info.flags);
		assertEquals(VkSampleCount.COUNT_8, info.rasterizationSamples);
		assertEquals(VulkanBoolean.TRUE, info.sampleShadingEnable);
		assertEquals(0.5f, info.minSampleShading);
		assertEquals(new IntegerArray(new int[1]), info.pSampleMask);
		assertEquals(VulkanBoolean.TRUE, info.alphaToCoverageEnable);
		assertEquals(VulkanBoolean.TRUE, info.alphaToOneEnable);
	}

	@Test
	void buildDefaults() {
		final VkPipelineMultisampleStateCreateInfo info = builder.get();
		assertNotNull(info);
		assertEquals(0, info.flags);
		assertEquals(VkSampleCount.COUNT_1, info.rasterizationSamples);
		assertEquals(VulkanBoolean.FALSE, info.sampleShadingEnable);
		assertEquals(1, info.minSampleShading);
		assertEquals(null, info.pSampleMask);
		assertEquals(VulkanBoolean.FALSE, info.alphaToCoverageEnable);
		assertEquals(VulkanBoolean.FALSE, info.alphaToOneEnable);
	}
}
