package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkGraphicsPipelineCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkPipelineCreateFlag;
import org.sarge.jove.platform.vulkan.VkPipelineShaderStageCreateInfo;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline.Builder.ShaderStageBuilder;
import org.sarge.jove.platform.vulkan.pipeline.Shader.ConstantsTable;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class PipelineTest extends AbstractVulkanTest {
	private Pipeline pipeline;
	private PipelineLayout layout;

	@BeforeEach
	void before() {
		layout = mock(PipelineLayout.class);
		pipeline = new Pipeline(new Pointer(1), dev, layout, Set.of(VkPipelineCreateFlag.ALLOW_DERIVATIVES));
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

		private void init() {
			builder
					.layout(layout)
					.pass(pass)
					.viewport(viewport)
					.shader(VkShaderStage.VERTEX)
						.shader(mock(Shader.class))
						.build();
		}

		@Test
		void build() {
			// Create pipeline cache
			final PipelineCache cache = mock(PipelineCache.class);

			// Build pipeline
			init();
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
				final ConstantsTable constants = new ConstantsTable();
				constants.add(1, 2);
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
				assertNotNull(info.pSpecializationInfo);
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
			@Test
			void derivative() {
				init();
				final Pipeline derivative = builder.derive(pipeline).build(null, dev);
				assertNotNull(derivative);
				assertTrue(derivative.flags().contains(VkPipelineCreateFlag.DERIVATIVE));
			}

			@Test
			void derivativeInvalidBasePipeline() {
				final Pipeline base = mock(Pipeline.class);
				when(base.flags()).thenReturn(Set.of());
				assertThrows(IllegalArgumentException.class, () -> builder.derive(base));
			}

			@Test
			void allowDerivatives() {
				init();
				builder.allowDerivatives();
				pipeline = builder.build(null, dev);
				assertTrue(pipeline.flags().contains(VkPipelineCreateFlag.ALLOW_DERIVATIVES));
			}

			@Test
			void derive() {
				// Create a pipeline that can be derived from
				init();
				builder.allowDerivatives();

				// Derive from this pipeline
				final Pipeline.Builder derived = builder.derive();
				assertNotNull(derived);
				assertNotSame(derived, builder);

				// Check that shaders can be overridden
				addVertexShaderStage(derived);

				// Construct pipelines
				final List<Pipeline> pipelines = Pipeline.Builder.build(List.of(derived), null, dev);
				assertNotNull(pipelines);
				assertEquals(2, pipelines.size());

				// Check derived pipeline
				pipeline = pipelines.get(0);
				assertNotNull(pipeline);
				assertTrue(pipeline.flags().contains(VkPipelineCreateFlag.DERIVATIVE));
			}
		}
	}
}
