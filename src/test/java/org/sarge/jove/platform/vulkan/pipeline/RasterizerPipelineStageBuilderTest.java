package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;

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
		assertEquals(0, info.flags);
		assertEquals(true, info.depthClampEnable);
		assertEquals(true, info.rasterizerDiscardEnable);
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
		assertEquals(0, info.flags);
		assertEquals(false, info.depthClampEnable);
		assertEquals(false, info.rasterizerDiscardEnable);
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
