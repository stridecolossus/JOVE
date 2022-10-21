package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
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
		pipeline = new Pipeline(new Handle(1), dev, layout, Set.of(VkPipelineCreateFlag.ALLOW_DERIVATIVES));
	}

	@Test
	void constructor() {
		assertEquals(layout, pipeline.layout());
		assertEquals(Set.of(VkPipelineCreateFlag.ALLOW_DERIVATIVES), pipeline.flags());
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

		private void init(Pipeline.Builder builder) {
			builder
					.layout(layout)
					.pass(pass)
					.viewport(viewport)
					.shader(VkShaderStage.VERTEX, mock(Shader.class));
		}

		@Test
		void build() {
			// Create pipeline cache
			final PipelineCache cache = mock(PipelineCache.class);

			// Build pipeline
			init(builder);
			pipeline = builder.build(cache, dev);

			// Check pipeline
			assertNotNull(pipeline);
			assertNotNull(pipeline.handle());
			assertEquals(false, pipeline.isDestroyed());
			assertEquals(layout, pipeline.layout());

			// Init expected descriptor
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

		private void addVertexShaderStage(Pipeline.Builder builder) {
			builder
				.shader(VkShaderStage.VERTEX)
					.shader(mock(Shader.class))
					.build();
		}

		@Test
		void buildIncomplete() {
			// Check empty builder
			assertThrows(IllegalArgumentException.class, "pipeline layout", () -> builder.build(null, dev));

			// Add layout
			builder.layout(layout);
			assertThrows(IllegalArgumentException.class, "render pass", () -> builder.build(null, dev));

			// Add render-pass
			builder.pass(pass);
			assertThrows(IllegalStateException.class, "vertex shader", () -> builder.build(null, dev));

			// Add shader
			addVertexShaderStage(builder);
			assertThrows(IllegalArgumentException.class, "viewports", () -> builder.build(null, dev));

			// Add viewport stage, should now build successfully
			builder
				.viewport()
					.viewport(viewport)
					.scissor(viewport)
					.build()
				.build(null, dev);
		}

		@Nested
		class ShaderStageBuilderTests {
			@Test
			void shader() {
				// Start shader stage
				final ShaderStageBuilder stage = builder.shader(VkShaderStage.VERTEX);
				assertNotNull(stage);

				// Configure method
				stage.name("name");

				// Configure vertex shader
				final Shader shader = mock(Shader.class);
				when(shader.handle()).thenReturn(new Handle(1));
				stage.shader(shader);

				// Configure constants
				final var constants = new VkSpecializationInfo();
				stage.constants(constants);

				// Check returns to parent
				assertEquals(builder, stage.build());

				// Check shader descriptor
				final var info = new VkPipelineShaderStageCreateInfo();
				stage.populate(info);
				assertEquals(0, info.flags);
				assertEquals(VkShaderStage.VERTEX, info.stage);
				assertEquals("name", info.pName);
				assertEquals(shader.handle(), info.module);
				assertEquals(constants, info.pSpecializationInfo);
			}

			@Test
			void shaderMissingShaderModule() {
				assertThrows(IllegalArgumentException.class, () -> builder.shader(VkShaderStage.VERTEX).build());
			}

			@Test
			void shaderDuplicateStage() {
				addVertexShaderStage(builder);
				assertThrows(IllegalArgumentException.class, () -> addVertexShaderStage(builder));
			}
		}

		@Nested
		class DerivativePipelineTests {
			@DisplayName("A pipeline can be derived from an existing parent pipeline")
			@Test
			void derivative() {
				init(builder);
				final Pipeline derivative = builder.derive(pipeline).build(null, dev);
				assertTrue(derivative.flags().contains(VkPipelineCreateFlag.DERIVATIVE));
			}

			@DisplayName("A pipeline cannot be derived from an existing parent that does not allow derivatives")
			@Test
			void invalid() {
				final Pipeline base = mock(Pipeline.class);
				assertThrows(IllegalArgumentException.class, () -> builder.derive(base));
			}

			@DisplayName("A pipeline cannot be derived more than once")
			@Test
			void duplicate() {
				builder.derive(pipeline);
				assertThrows(IllegalArgumentException.class, () -> builder.derive(pipeline));
			}

			@DisplayName("A pipeline can be configured as a parent")
			@Test
			void parent() {
				init(builder);
				builder.parent();
				pipeline = builder.build(null, dev);
				assertTrue(pipeline.flags().contains(VkPipelineCreateFlag.ALLOW_DERIVATIVES));
			}

			@DisplayName("A pipeline cannot be derived from itself")
			@Test
			void self() {
				assertThrows(IllegalStateException.class, () -> builder.derive(builder));
			}

			@DisplayName("A pipeline can be derived from another builder")
			@Test
			void indexed() {
				// Init parent builder
				init(builder);
				builder.parent();

				// Create derived builder
				final var child = new Pipeline.Builder();
				init(child);
				child.derive(builder);

				// Construct pipelines
				Pipeline.Builder.build(List.of(child, builder), null, dev);
			}

			@DisplayName("A pipeline cannot be derived from another builder that is not present at instantiation")
			@Test
			void missing() {
				final var child = new Pipeline.Builder();
				init(child);
				builder.parent();
				child.derive(builder);
				assertThrows(IllegalArgumentException.class, () -> child.build(null, dev));
			}
		}
	}
}
