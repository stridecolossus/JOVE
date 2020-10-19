package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkCompareOp;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

public class DepthStencilStageBuilderTest {
	private DepthStencilStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new DepthStencilStageBuilder();
	}

	@Test
	void build() {
		// Build descriptor
		final var result = builder
				.enable(true)
				.write(false)
				.compare(VkCompareOp.VK_COMPARE_OP_GREATER)
				.result();

		// Check descriptor
		assertNotNull(result);
		assertEquals(VulkanBoolean.TRUE, result.depthTestEnable);
		assertEquals(VulkanBoolean.FALSE, result.depthWriteEnable);
		assertEquals(VkCompareOp.VK_COMPARE_OP_GREATER, result.depthCompareOp);
		assertEquals(VulkanBoolean.FALSE, result.stencilTestEnable);
		// TODO
	}

	@Test
	void buildDefaults() {
		final var result = builder.result();
		assertNotNull(result);
		assertEquals(VulkanBoolean.FALSE, result.depthTestEnable);
		assertEquals(VulkanBoolean.TRUE, result.depthWriteEnable);
		assertEquals(VkCompareOp.VK_COMPARE_OP_LESS, result.depthCompareOp);
		assertEquals(VulkanBoolean.FALSE, result.stencilTestEnable);
		// TODO
	}
}
