package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.IntegerArray;
import org.sarge.lib.util.Percentile;

public class MultiSampleStageBuilderTest {
	private MultiSampleStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new MultiSampleStageBuilder();
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
		assertEquals(true, info.sampleShadingEnable);
		assertEquals(0.5f, info.minSampleShading);
		assertEquals(new IntegerArray(new int[1]), info.pSampleMask);
		assertEquals(true, info.alphaToCoverageEnable);
		assertEquals(true, info.alphaToOneEnable);
	}

	@Test
	void buildDefaults() {
		final VkPipelineMultisampleStateCreateInfo info = builder.get();
		assertNotNull(info);
		assertEquals(0, info.flags);
		assertEquals(VkSampleCount.COUNT_1, info.rasterizationSamples);
		assertEquals(false, info.sampleShadingEnable);
		assertEquals(1, info.minSampleShading);
		assertEquals(null, info.pSampleMask);
		assertEquals(false, info.alphaToCoverageEnable);
		assertEquals(false, info.alphaToOneEnable);
	}
}
