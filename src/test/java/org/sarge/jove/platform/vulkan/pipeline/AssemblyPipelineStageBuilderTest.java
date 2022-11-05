package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.vulkan.*;

class AssemblyPipelineStageBuilderTest {
	private AssemblyPipelineStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new AssemblyPipelineStageBuilder();
	}

	@Test
	void build() {
		// Build descriptor
		final VkPipelineInputAssemblyStateCreateInfo info = builder
				.topology(Primitive.LINES)
				.restart(true)
				.get();

		// Check descriptor
		assertEquals(0, info.flags);
		assertEquals(VkPrimitiveTopology.LINE_LIST, info.topology);
		assertEquals(true, info.primitiveRestartEnable);
	}

	@Test
	void buildDefault() {
		final VkPipelineInputAssemblyStateCreateInfo info = builder.get();
		assertEquals(VkPrimitiveTopology.TRIANGLE_STRIP, info.topology);
		assertEquals(false, info.primitiveRestartEnable);
	}

	@ParameterizedTest
	@EnumSource(Primitive.class)
	void primitives(Primitive primitive) {
		builder.topology(primitive);
	}
}
