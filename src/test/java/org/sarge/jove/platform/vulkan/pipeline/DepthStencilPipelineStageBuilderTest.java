package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkCompareOp;

public class DepthStencilPipelineStageBuilderTest {
	private DepthStencilPipelineStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new DepthStencilPipelineStageBuilder();
	}

	@Test
	void build() {
		// Build descriptor
		final var result = builder
				.enable(true)
				.write(false)
				.compare(VkCompareOp.GREATER)
				.get();

		// Check descriptor
		assertEquals(0, result.flags);
		assertEquals(true, result.depthTestEnable);
		assertEquals(false, result.depthWriteEnable);
		assertEquals(VkCompareOp.GREATER, result.depthCompareOp);
		//assertEquals(false, result.stencilTestEnable);
	}

	@Test
	void buildDefaults() {
		final var result = builder.get();
		assertEquals(0, result.flags);
		assertEquals(false, result.depthTestEnable);
		assertEquals(true, result.depthWriteEnable);
		assertEquals(VkCompareOp.LESS_OR_EQUAL, result.depthCompareOp);
		//assertEquals(false, result.stencilTestEnable);
	}
}
