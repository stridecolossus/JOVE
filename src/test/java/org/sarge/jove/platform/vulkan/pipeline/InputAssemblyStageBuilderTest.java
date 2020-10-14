package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.vulkan.VkPrimitiveTopology;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

public class InputAssemblyStageBuilderTest {
	private InputAssemblyStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new InputAssemblyStageBuilder();
	}

	@Test
	void build() {
		// Build descriptor
		final var info = builder
				.topology(Primitive.LINE_LIST)
				.restart(true)
				.result();

		// Check descriptor
		assertNotNull(info);
		assertEquals(VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_LINE_LIST, info.topology);
		assertEquals(VulkanBoolean.TRUE, info.primitiveRestartEnable);
	}

	@Test
	void buildDefault() {
		final var info = builder.result();
		assertEquals(VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP, info.topology);
		assertEquals(VulkanBoolean.FALSE, info.primitiveRestartEnable);
	}
}
