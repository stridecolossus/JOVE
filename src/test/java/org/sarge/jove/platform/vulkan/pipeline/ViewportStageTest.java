package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.pipeline.ViewportStage.Viewport;

class ViewportStageTest {
	private ViewportStage stage;
	private Rectangle rectangle;
	private Viewport viewport;

	@BeforeEach
	void before() {
		stage = new ViewportStage();
		rectangle = new Rectangle(0, 0, 640, 480);
		viewport = new Viewport(rectangle);
	}

	@Test
	void build() {
		// Build descriptor
		final VkPipelineViewportStateCreateInfo descriptor = stage
				.viewport(viewport)
				.scissor(rectangle)
				.descriptor();

		// Check descriptor
		assertEquals(0, descriptor.flags);
		assertEquals(1, descriptor.viewportCount);
		assertEquals(1, descriptor.scissorCount);

		// Check viewports
		final VkViewport viewport = descriptor.pViewports[0];
		assertEquals(1, viewport.x);
		assertEquals(2, viewport.y);
		assertEquals(3, viewport.width);
		assertEquals(4, viewport.height);
		assertEquals(0, viewport.minDepth);
		assertEquals(1, viewport.maxDepth);

		// Check scissors
		final VkRect2D scissor = descriptor.pScissors[0];
		assertEquals(1, scissor.offset.x);
		assertEquals(2, scissor.offset.y);
		assertEquals(3, scissor.extent.width);
		assertEquals(4, scissor.extent.height);
	}

	@DisplayName("The viewport coordinate system can be flipped vertically")
	@Test
	void flip() {
		final Viewport flip = viewport.flip();
		assertEquals(new Rectangle(1, 2 + 4, 3, -4), flip.rectangle());
	}

	@DisplayName("The viewport configuration cannot be empty")
	@Test
	void empty() {
		assertThrows(IllegalArgumentException.class, () -> stage.descriptor());
	}

	@DisplayName("The number of scissor rectangles must match the number of viewports")
	@Test
	void scissor() {
		stage.viewport(viewport);
		assertThrows(IllegalArgumentException.class, () -> stage.descriptor());
	}
}

//
//	@Nested
//	class DynamicState {
//		private VulkanLibrary lib;
//		private Command.CommandBuffer buffer;
//
//		@BeforeEach
//		void before() {
//			lib = mock(VulkanLibrary.class);
//			buffer = mock(Command.CommandBuffer.class);
//		}
//
//		@DisplayName("A viewport can be configured dynamically")
//    	@Test
//    	void viewport() {
//    		// Init viewport
//    		stage.viewport(viewport);
//
//    		// Init expected viewport descriptor
//    		final var info = new VkViewport() {
//    			@Override
//    			public boolean equals(Object obj) {
//    				return dataEquals((VkViewport) obj);
//    			}
//    		};
//    		info.x = 1;
//    		info.y = 2;
//    		info.width = 3;
//    		info.height = 4;
//    		info.minDepth = 0;
//    		info.maxDepth = 1;
//
//    		// Execute dynamic viewport command
//    		final Command cmd = stage.setDynamicViewport(0, List.of(viewport));
//    		cmd.execute(lib, buffer);
//    		verify(lib).vkCmdSetViewport(buffer, 0, 1, info);
//    	}
//
//		@DisplayName("A dynamic viewport must override the pipeline stage configuration")
//    	@Test
//    	void viewportInvalid() {
//    		assertThrows(IllegalArgumentException.class, () -> stage.setDynamicViewport(0, List.of()));
//    		assertThrows(IllegalArgumentException.class, () -> stage.setDynamicViewport(1, List.of(viewport)));
//    	}
//
//		@DisplayName("A scissor rectangle can be configured dynamically")
//    	@Test
//    	void scissor() {
//    		// Init viewport and scissor
//    		stage.viewport(viewport);
//    		stage.scissor(rect);
//
//    		// Init expected scissor rectangle
//    		final var info = new VkRect2D() {
//    			@Override
//    			public boolean equals(Object obj) {
//    				return dataEquals((VkRect2D) obj);
//    			}
//    		};
//    		info.offset.x = 1;
//    		info.offset.y = 2;
//    		info.extent.width = 3;
//    		info.extent.height = 4;
//
//    		// Execute dynamic scissor command
//    		final Command cmd = stage.setDynamicScissor(0, List.of(rect));
//    		cmd.execute(lib, buffer);
//    		verify(lib).vkCmdSetScissor(buffer, 0, 1, info);
//    	}
//
//		@DisplayName("A dynamic scissor must override the pipeline stage configuration")
//    	@Test
//    	void scissorInvalid() {
//    		assertThrows(IllegalArgumentException.class, () -> stage.setDynamicScissor(0, List.of()));
//    		assertThrows(IllegalArgumentException.class, () -> stage.setDynamicScissor(1, List.of(rect)));
//    	}
//	}
//}
