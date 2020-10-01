package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkCullModeFlag;
import org.sarge.jove.platform.vulkan.VkFrontFace;
import org.sarge.jove.platform.vulkan.VkPolygonMode;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

public class RasterizerStageBuilderTest {
	private RasterizerStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new RasterizerStageBuilder();
	}

	@Test
	void create() {
		// Create a shader stage
		final var info = builder
				.depthClampEnable(true)
				.discardEnable(true)
				.polygonMode(VkPolygonMode.VK_POLYGON_MODE_LINE)
				.cullMode(VkCullModeFlag.VK_CULL_MODE_FRONT_AND_BACK)
				.frontFace(true)
				.lineWidth(2)
				.result();

		// Check descriptor
		assertNotNull(info);
		assertEquals(0, info.flags);
		assertEquals(VulkanBoolean.TRUE, info.depthClampEnable);
		assertEquals(VulkanBoolean.TRUE, info.rasterizerDiscardEnable);
		assertEquals(VkPolygonMode.VK_POLYGON_MODE_LINE, info.polygonMode);
		assertEquals(VkCullModeFlag.VK_CULL_MODE_FRONT_AND_BACK, info.cullMode);
		assertEquals(VkFrontFace.VK_FRONT_FACE_CLOCKWISE, info.frontFace);
		assertEquals(2, info.lineWidth);

		// Check depth bias
		assertEquals(VulkanBoolean.FALSE, info.depthBiasEnable);
		assertEquals(0, info.depthBiasConstantFactor);
		assertEquals(0, info.depthBiasClamp);
		assertEquals(0, info.depthBiasSlopeFactor);
	}
}