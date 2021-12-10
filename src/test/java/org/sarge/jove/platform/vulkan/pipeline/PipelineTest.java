package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkGraphicsPipelineCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkPipelineShaderStageCreateInfo;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline.Builder.ShaderStageBuilder;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class PipelineTest extends AbstractVulkanTest {
	private Pipeline pipeline;
	private PipelineLayout layout;

	@BeforeEach
	void before() {
		layout = mock(PipelineLayout.class);
		pipeline = new Pipeline(new Pointer(1), dev, layout);
	}

	@Test
	void constructor() {
		assertEquals(layout, pipeline.layout());
		assertEquals(false, pipeline.isDestroyed());
	}

	@Test
	void bind() {
		// Create command
		final Command cmd = pipeline.bind();
		assertNotNull(cmd);

		// Check bind pipeline
		final Command.Buffer cb = mock(Command.Buffer.class);
		cmd.execute(lib, cb);
		verify(lib).vkCmdBindPipeline(cb, VkPipelineBindPoint.GRAPHICS, pipeline);
	}

	@Test
	void destroy() {
		pipeline.destroy();
		verify(lib).vkDestroyPipeline(dev, pipeline, null);
	}

	@Nested
	class BuilderTests {
		private Pipeline.Builder builder;
		private RenderPass pass;
		private Rectangle viewport;

		@BeforeEach
		void before() {
			builder = new Pipeline.Builder();
			pass = mock(RenderPass.class);
			viewport = new Rectangle(new Dimensions(3, 4));
		}

		@Test
		void builders() {
			assertNotNull(builder.input());
			assertNotNull(builder.assembly());
			assertNotNull(builder.tesselation());
			assertNotNull(builder.viewport());
			assertNotNull(builder.rasterizer());
			assertNotNull(builder.depth());
			assertNotNull(builder.blend());
			assertNotNull(builder.dynamic());
		}

		@Test
		void build() {
			// Create pipeline cache
			final PipelineCache cache = mock(PipelineCache.class);

			// Build pipeline
			pipeline = builder
					.layout(layout)
					.pass(pass)
					.cache(cache)
					.viewport()
						.viewport(viewport)
						.scissor(viewport)
						.build()
					.shader(VkShaderStage.VERTEX)
						.shader(mock(Shader.class))
						.build()
					.build(dev);

			// Check pipeline
			assertNotNull(pipeline);

			final var expected = new VkGraphicsPipelineCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					// Check descriptor
					final var info = (VkGraphicsPipelineCreateInfo) obj;
					assertNotNull(info);
					assertEquals(0, info.flags);

					// Check derived pipelines
					assertEquals(null, info.basePipelineHandle);
					assertEquals(-1, info.basePipelineIndex);

					// Check render pass
					assertEquals(0, info.subpass);

					// Check mandatory stage descriptors
					assertNotNull(info.pVertexInputState);
					assertNotNull(info.pInputAssemblyState);
					assertNull(info.pTessellationState);
					assertNotNull(info.pViewportState);
					assertNotNull(info.pRasterizationState);
					assertNotNull(info.pMultisampleState);
					assertNotNull(info.pColorBlendState);
					assertNull(info.pDynamicState);

					// Check viewport stage
					assertEquals(1, info.pViewportState.viewportCount);
					assertEquals(1, info.pViewportState.scissorCount);
					assertNotNull(info.pViewportState.pViewports);
					assertNotNull(info.pViewportState.pScissors);

					// Check shader stage descriptor
					assertEquals(1, info.stageCount);
					assertNotNull(info.pStages);

					return true;
				}
			};

			// Check API
			verify(lib).vkCreateGraphicsPipelines(dev, cache, 1, new VkGraphicsPipelineCreateInfo[]{expected}, null, new Pointer[1]);
		}

		private void addVertexShaderStage() {
			builder
				.shader(VkShaderStage.VERTEX)
					.shader(mock(Shader.class))
					.build();
		}

		@Test
		void buildIncomplete() {
			// Check empty builder
			assertThrows(IllegalArgumentException.class, "pipeline layout", () -> builder.build(dev));

			// Add layout
			builder.layout(layout);
			assertThrows(IllegalArgumentException.class, "render pass", () -> builder.build(dev));

			// Add render-pass
			builder.pass(pass);
			assertThrows(IllegalStateException.class, "vertex shader", () -> builder.build(dev));

			// Add shader
			addVertexShaderStage();
			assertThrows(IllegalArgumentException.class, "viewports", () -> builder.build(dev));

			// Add viewport stage, should now build successfully
			builder
				.viewport()
					.viewport(viewport)
					.scissor(viewport)
					.build()
				.build(dev);
		}

		@Test
		void shader() {
			// Start shader stage
			final ShaderStageBuilder stage = builder.shader(VkShaderStage.VERTEX);
			assertNotNull(stage);

			// Configure vertex shader
			final Shader shader = mock(Shader.class);
			when(shader.handle()).thenReturn(new Handle(1));
			stage.name("name").shader(shader);

			// Check returns to parent
			assertEquals(builder, stage.build());

			// Check shader descriptor
			final var info = new VkPipelineShaderStageCreateInfo();
			stage.populate(info);
			assertEquals(0, info.flags);
			assertEquals(VkShaderStage.VERTEX, info.stage);
			assertEquals("name", info.pName);
			assertEquals(shader.handle(), info.module);
		}

		@Test
		void shaderMissingShaderModule() {
			assertThrows(IllegalArgumentException.class, () -> builder.shader(VkShaderStage.VERTEX).build());
		}

		@Test
		void shaderDuplicateStage() {
			addVertexShaderStage();
			assertThrows(IllegalArgumentException.class, () -> addVertexShaderStage());
		}
	}
}
