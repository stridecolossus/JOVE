package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.render.MockRenderPass;
import org.sarge.jove.util.*;

class GraphicsPipelineBuilderTest {
	@SuppressWarnings("unused")
	private static class MockCreatePipelineLibrary extends MockLibrary {
		public VkResult vkCreateGraphicsPipelines(LogicalDevice device, PipelineCache pipelineCache, int createInfoCount, VkGraphicsPipelineCreateInfo[] pCreateInfos, Handle pAllocator, Handle[] pPipelines) {
			// Check configuration
			assertEquals(null, pipelineCache);
			assertEquals(createInfoCount, pCreateInfos.length);

			// Check pipeline descriptor
			assertEquals(new EnumMask<>(), pCreateInfos[0].flags);
			assertNotNull(pCreateInfos[0].layout);
			assertNotNull(pCreateInfos[0].renderPass);
			assertEquals(0, pCreateInfos[0].subpass);

			// Check viewport stage configured
			assertNotNull(pCreateInfos[0].pViewportState);

			// Check vertex shader provided
			final var vertex = new EnumMask<>(VkShaderStageFlags.VERTEX);
			assertNotEquals(0, pCreateInfos[0].stageCount);
			assertEquals(true, Arrays.stream(pCreateInfos[0].pStages).map(e -> e.stage).anyMatch(vertex::equals));

			// Mock pipeline
			assertEquals(1, pPipelines.length);
			init(pPipelines);
			return VkResult.VK_SUCCESS;
		}
	}

	private GraphicsPipelineBuilder builder;
	private LogicalDevice device;
	private ProgrammableShaderStage shader;
	private Mockery mockery;

	@BeforeEach
	void before() {
		mockery = new Mockery(new MockCreatePipelineLibrary(), Pipeline.Library.class);
		device = new MockLogicalDevice(mockery.proxy());
		shader = ProgrammableShaderStage.of(VkShaderStageFlags.VERTEX, new MockShader());
		builder = new GraphicsPipelineBuilder();
		builder.layout(new MockPipelineLayout());
		builder.pass(new MockRenderPass());

	}

	@Test
	void build() {
		// Init shader
		builder.shader(shader);

		// Add viewport and scissor
		builder
				.viewport()
				.viewportAndScissor(new Rectangle(0, 0, 640, 480));

		// Create pipeline
		final Pipeline pipeline = builder.build(device);
		assertEquals(new Handle(1), pipeline.handle());
		assertEquals(device, pipeline.device());
		assertEquals(false, pipeline.isParent());
		assertNotNull(pipeline.layout());

	}

	@DisplayName("A graphics pipeline requires a vertex shader")
	@Test
	void shader() {
		assertThrows(IllegalStateException.class, () -> builder.build(device));
	}

	@DisplayName("A programmable shader stage cannot be added more than once")
	@Test
	void duplicate() {
		builder.shader(shader);
		assertThrows(IllegalStateException.class, () -> builder.shader(shader));
	}

	@DisplayName("A graphics pipeline requires at least one viewport and scissor rectangle")
	@Test
	void viewport() {
		builder.shader(shader);
		assertThrows(IllegalStateException.class, () -> builder.build(device));
	}
}
