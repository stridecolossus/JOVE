package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.vulkan.*;

class InputAssemblyStageTest {
	private InputAssemblyStage builder;

	@BeforeEach
	void before() {
		builder = new InputAssemblyStage();
	}

	@Test
	void build() {
		// Build descriptor
		final VkPipelineInputAssemblyStateCreateInfo info = builder
				.topology(Primitive.LINE)
				.restart(true)
				.descriptor();

		// Check descriptor
		assertEquals(0, info.flags);
		assertEquals(VkPrimitiveTopology.LINE_LIST, info.topology);
		assertEquals(true, info.primitiveRestartEnable);
	}

	@Test
	void defaults() {
		final VkPipelineInputAssemblyStateCreateInfo info = builder.descriptor();
		assertEquals(0, info.flags);
		assertEquals(VkPrimitiveTopology.TRIANGLE_STRIP, info.topology);
		assertEquals(false, info.primitiveRestartEnable);
	}
}
