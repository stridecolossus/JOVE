package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkGraphicsPipelineCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.util.MockReferenceFactory;

import com.sun.jna.Pointer;

public class PipelineTest {
	private Pipeline pipeline;
	private LogicalDevice dev;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		// Create API
		lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(new MockReferenceFactory());

		// Create device
		dev = mock(LogicalDevice.class);
		when(dev.library()).thenReturn(lib);

		// Create pipeline
		pipeline = new Pipeline(new Pointer(42), dev);
	}

	@Test
	void destroy() {
		pipeline.destroy();
		verify(lib).vkDestroyPipeline(dev.handle(), new Pointer(42), null);
	}

	@Nested
	class BuilderTests {
		private Pipeline.Builder builder;
		private Rectangle rect;

		@BeforeEach
		void before() {
			builder = new Pipeline.Builder(dev);
			rect = new Rectangle(0, 0, 3, 4); // TODO - helper ctor
		}

		@Test
		void builders() {
			assertNotNull(builder.input());
			assertNotNull(builder.viewport());
		}

		@Test
		void build() {
			// Build pipeline
			pipeline = builder
					.viewport()
						.viewport(rect)
						.scissor(rect)
						.build()
					.build();

			// Check pipeline
			assertNotNull(pipeline);

			// Check allocation
			final ArgumentCaptor<VkGraphicsPipelineCreateInfo[]> captor = ArgumentCaptor.forClass(VkGraphicsPipelineCreateInfo[].class);
			verify(lib).vkCreateGraphicsPipelines(eq(dev.handle()), isNull(), eq(1), captor.capture(), isNull(), isA(Pointer[].class));
			assertEquals(1, captor.getValue().length);

			// Check descriptor
			final VkGraphicsPipelineCreateInfo info = captor.getValue()[0];
			assertNotNull(info);
			assertNotNull(info.pVertexInputState);
		}

		@Test
		void buildRequiresVertexShaderStage() {
			builder.viewport().viewport(rect).scissor(rect).build();
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildRequiresViewportStage() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}
