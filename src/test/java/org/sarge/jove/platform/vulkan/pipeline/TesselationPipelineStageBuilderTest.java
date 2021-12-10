package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkPipelineTessellationStateCreateInfo;

public class TesselationPipelineStageBuilderTest {
	private TesselationPipelineStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new TesselationPipelineStageBuilder(null);
	}

	@Test
	void build() {
		final VkPipelineTessellationStateCreateInfo info = builder.points(3).get();
		assertNotNull(info);
		assertEquals(0, info.flags);
		assertEquals(3, info.patchControlPoints);
	}
}
