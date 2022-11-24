package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkPipelineTessellationStateCreateInfo;

public class TesselationStageBuilderTest {
	private TesselationStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new TesselationStageBuilder();
	}

	@Test
	void build() {
		final VkPipelineTessellationStateCreateInfo info = builder.points(3).get();
		assertEquals(0, info.flags);
		assertEquals(3, info.patchControlPoints);
	}

	@Test
	void optional() {
		assertEquals(null, builder.get());
	}
}
