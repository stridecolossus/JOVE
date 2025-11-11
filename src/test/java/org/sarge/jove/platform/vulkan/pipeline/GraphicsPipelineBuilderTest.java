package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.render.*;
import org.sarge.jove.util.EnumMask;

class GraphicsPipelineBuilderTest {
	private static class MockPipelineLibrary extends MockVulkanLibrary {
		@Override
		public VkResult vkCreateGraphicsPipelines(LogicalDevice device, PipelineCache pipelineCache, int createInfoCount, VkGraphicsPipelineCreateInfo[] pCreateInfos, Handle pAllocator, Handle[] pPipelines) {
			// Check configuration
			assertNotNull(device);
			assertEquals(null, pipelineCache);
			assertEquals(1, createInfoCount);
			assertEquals(1, pCreateInfos.length);

			// Check pipeline descriptor
			assertEquals(new EnumMask<>(), pCreateInfos[0].flags);
			assertNotNull(pCreateInfos[0].layout);
			assertNotNull(pCreateInfos[0].renderPass);
			assertEquals(0, pCreateInfos[0].subpass);

			// Check viewport stage configured
			assertNotNull(pCreateInfos[0].pViewportState);

			// Check vertex shader provided
			assertNotEquals(0, pCreateInfos[0].stageCount);
			assertEquals(true, Arrays.stream(pCreateInfos[0].pStages).map(e -> e.stage).anyMatch(VkShaderStage.VERTEX::equals));

			// Mock pipeline
			assertEquals(1, pPipelines.length);
			pPipelines[0] = new Handle(1);
			return VkResult.SUCCESS;
		}
	}

	private GraphicsPipelineBuilder builder;
	private LogicalDevice device;
	private MockPipelineLibrary library;
	private PipelineLayout layout;
	private RenderPass pass;
	private ProgrammableShaderStage shader;

	@BeforeEach
	void before() {
		library = new MockPipelineLibrary();
		device = new MockLogicalDevice(library);
		layout = new PipelineLayout(new Handle(2), device, null);
		pass = new MockRenderPass(device);
		shader = ProgrammableShaderStage.of(VkShaderStage.VERTEX, new MockShader(device));
		builder = new GraphicsPipelineBuilder();
	}

	@Test
	void build() {
		// Init pipeline
		builder
				.layout(layout)
				.pass(pass)
				.shader(shader);

		// Add viewport and scissor
		builder
				.viewport()
				.viewportAndScissor(new Rectangle(0, 0, 640, 480));

		// Create pipeline
		final Pipeline pipeline = builder.build(device);
		assertEquals(new Handle(1), pipeline.handle());
		assertEquals(device, pipeline.device());
		assertEquals(layout, pipeline.layout());
		assertEquals(false, pipeline.isParent());

	}

	@DisplayName("A graphics pipeline requires a vertex shader")
	@Test
	void shader() {
		builder
        		.layout(layout)
        		.pass(pass);

		assertThrows(IllegalStateException.class, () -> builder.build(device));
	}

	@DisplayName("A programmable shader stage cannot be added more than once")
	@Test
	void duplicate() {
		builder
        		.layout(layout)
        		.pass(pass)
        		.shader(shader);

		assertThrows(IllegalStateException.class, () -> builder.shader(shader));
	}

	@DisplayName("A graphics pipeline requires at least one viewport and scissor rectangle")
	@Test
	void viewport() {
		builder
        		.layout(layout)
        		.pass(pass)
        		.shader(shader);

		assertThrows(IllegalStateException.class, () -> builder.build(device));
	}
}
