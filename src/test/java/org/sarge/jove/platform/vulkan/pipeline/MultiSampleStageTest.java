package org.sarge.jove.platform.vulkan.pipeline;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.*;

public class MultiSampleStageTest {
	private MultiSampleStage stage;

	@BeforeEach
	void before() {
		stage = new MultiSampleStage();
	}

	@Test
	void build() {
		final VkPipelineMultisampleStateCreateInfo info = stage
				.samples(8)
				.sampleShadingEnable(true)
				.minSampleShading(Percentile.HALF)
				.sampleMask(new int[1])
				.alphaToCoverageEnable(true)
				.alphaToOneEnable(true)
				.descriptor();

		assertEquals(0, info.flags);
		assertEquals(new EnumMask<>(VkSampleCountFlags.COUNT_8), info.rasterizationSamples);
		assertEquals(true, info.sampleShadingEnable);
		assertEquals(0.5f, info.minSampleShading);
		assertArrayEquals(new int[1], info.pSampleMask);
		assertEquals(true, info.alphaToCoverageEnable);
		assertEquals(true, info.alphaToOneEnable);
	}

	@Test
	void defaults() {
		final VkPipelineMultisampleStateCreateInfo info = stage.descriptor();
		assertEquals(0, info.flags);
		assertEquals(new EnumMask<>(VkSampleCountFlags.COUNT_1), info.rasterizationSamples);
		assertEquals(false, info.sampleShadingEnable);
		assertEquals(1, info.minSampleShading);
		assertEquals(null, info.pSampleMask);
		assertEquals(false, info.alphaToCoverageEnable);
		assertEquals(false, info.alphaToOneEnable);
	}
}
