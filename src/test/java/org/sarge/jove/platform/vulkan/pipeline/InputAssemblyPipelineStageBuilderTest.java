package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.vulkan.VkPrimitiveTopology;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

public class InputAssemblyPipelineStageBuilderTest {
	private InputAssemblyPipelineStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new InputAssemblyPipelineStageBuilder(null);
	}

	@Test
	void build() {
		// Build descriptor
		final var info = builder
				.topology(Primitive.LINES)
				.restart(true)
				.get();

		// Check descriptor
		assertNotNull(info);
		assertEquals(0, info.flags);
		assertEquals(VkPrimitiveTopology.LINE_LIST, info.topology);
		assertEquals(VulkanBoolean.TRUE, info.primitiveRestartEnable);
	}

	@Test
	void buildDefault() {
		final var info = builder.get();
		assertEquals(VkPrimitiveTopology.TRIANGLE_STRIP, info.topology);
		assertEquals(VulkanBoolean.FALSE, info.primitiveRestartEnable);
	}
}
