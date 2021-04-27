package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkCompareOp;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

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
		assertEquals(0, result.flags);
		assertEquals(VulkanBoolean.TRUE, result.depthTestEnable);
		assertEquals(VulkanBoolean.FALSE, result.depthWriteEnable);
		assertEquals(VkCompareOp.VK_COMPARE_OP_GREATER, result.depthCompareOp);
		assertEquals(VulkanBoolean.FALSE, result.stencilTestEnable);
	}

	@Test
	void buildDefaults() {
		final var result = builder.result();
		assertNotNull(result);
		assertEquals(0, result.flags);
		assertEquals(VulkanBoolean.FALSE, result.depthTestEnable);
		assertEquals(VulkanBoolean.TRUE, result.depthWriteEnable);
		assertEquals(VkCompareOp.VK_COMPARE_OP_LESS, result.depthCompareOp);
		assertEquals(VulkanBoolean.FALSE, result.stencilTestEnable);
	}
}
