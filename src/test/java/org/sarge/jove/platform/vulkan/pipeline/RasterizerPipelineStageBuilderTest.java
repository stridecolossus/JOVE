package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkCullMode;
import org.sarge.jove.platform.vulkan.VkFrontFace;
import org.sarge.jove.platform.vulkan.VkPolygonMode;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

public class RasterizerPipelineStageBuilderTest {
	private RasterizerPipelineStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new RasterizerPipelineStageBuilder();
	}

	@Test
	void build() {
		// Create a shader stage
		final var info = builder
				.depthClamp(true)
				.discard(true)
				.polygon(VkPolygonMode.LINE)
				.cull(VkCullMode.FRONT_AND_BACK)
				.winding(VkFrontFace.CLOCKWISE)
				.lineWidth(2)
				.get();

		// Check descriptor
		assertNotNull(info);
		assertEquals(0, info.flags);
		assertEquals(VulkanBoolean.TRUE, info.depthClampEnable);
		assertEquals(VulkanBoolean.TRUE, info.rasterizerDiscardEnable);
		assertEquals(VkPolygonMode.LINE, info.polygonMode);
		assertEquals(VkCullMode.FRONT_AND_BACK, info.cullMode);
		assertEquals(VkFrontFace.CLOCKWISE, info.frontFace);
		assertEquals(2, info.lineWidth);

//		// Check depth bias
//		assertEquals(VulkanBoolean.FALSE, info.depthBiasEnable);
//		assertEquals(0, info.depthBiasConstantFactor);
//		assertEquals(0, info.depthBiasClamp);
//		assertEquals(0, info.depthBiasSlopeFactor);
	}

	@Test
	void buildDefaults() {
		final var info = builder.get();
		assertNotNull(info);
		assertEquals(0, info.flags);
		assertEquals(VulkanBoolean.FALSE, info.depthClampEnable);
		assertEquals(VulkanBoolean.FALSE, info.rasterizerDiscardEnable);
		assertEquals(VkPolygonMode.FILL, info.polygonMode);
		assertEquals(VkCullMode.BACK, info.cullMode);
		assertEquals(VkFrontFace.COUNTER_CLOCKWISE, info.frontFace);
		assertEquals(1, info.lineWidth);
//		assertEquals(VulkanBoolean.FALSE, info.depthBiasEnable);
//		assertEquals(0, info.depthBiasConstantFactor);
//		assertEquals(0, info.depthBiasClamp);
//		assertEquals(0, info.depthBiasSlopeFactor);
	}
}
