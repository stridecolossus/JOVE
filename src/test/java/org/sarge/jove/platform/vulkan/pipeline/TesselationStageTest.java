package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkPipelineTessellationStateCreateInfo;

public class TesselationStageTest {
	private TesselationStage builder;

	@BeforeEach
	void before() {
		builder = new TesselationStage();
	}

	@Test
	void build() {
		final VkPipelineTessellationStateCreateInfo info = builder.points(3).descriptor();
		assertEquals(0, info.flags);
		assertEquals(3, info.patchControlPoints);
	}

	@Test
	void empty() {
		assertEquals(null, builder.descriptor());
	}
}
