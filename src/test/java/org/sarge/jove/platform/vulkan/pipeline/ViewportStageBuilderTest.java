package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.pipeline.ViewportStageBuilder.Viewport;

class ViewportStageBuilderTest {
	private ViewportStageBuilder builder;
	private Rectangle rect;
	private Viewport viewport;

	@BeforeEach
	void before() {
		builder = new ViewportStageBuilder();
		rect = new Rectangle(1, 2, 3, 4);
		viewport = new Viewport(rect);
	}

	@Test
	void build() {
		// Build descriptor
		final var descriptor = builder
				.viewport(viewport)
				.scissor(rect)
				.get();

		// Check descriptor
		assertEquals(0, descriptor.flags);
		assertEquals(1, descriptor.viewportCount);
		assertEquals(1, descriptor.scissorCount);

		// Check viewport
		assertEquals(1, descriptor.pViewports.x);
		assertEquals(2, descriptor.pViewports.y);
		assertEquals(3, descriptor.pViewports.width);
		assertEquals(4, descriptor.pViewports.height);
		assertEquals(0, descriptor.pViewports.minDepth);
		assertEquals(1, descriptor.pViewports.maxDepth);

		// Check scissor
		assertEquals(1, descriptor.pScissors.offset.x);
		assertEquals(2, descriptor.pScissors.offset.y);
		assertEquals(3, descriptor.pScissors.extent.width);
		assertEquals(4, descriptor.pScissors.extent.height);
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
		assertThrows(IllegalArgumentException.class, () -> builder.get());
	}

	@DisplayName("The number of scissor rectangles must match the number of viewports")
	@Test
	void scissor() {
		builder.viewport(viewport);
		assertThrows(IllegalArgumentException.class, () -> builder.get());
	}

	@Nested
	class DynamicState {
		private VulkanLibrary lib;
		private Command.Buffer buffer;

		@BeforeEach
		void before() {
			lib = mock(VulkanLibrary.class);
			buffer = mock(Command.Buffer.class);
		}

		@DisplayName("A viewport can be configured dynamically")
    	@Test
    	void viewport() {
    		// Init viewport
    		builder.viewport(viewport);

    		// Init expected viewport descriptor
    		final var info = new VkViewport() {
    			@Override
    			public boolean equals(Object obj) {
    				return dataEquals((VkViewport) obj);
    			}
    		};
    		info.x = 1;
    		info.y = 2;
    		info.width = 3;
    		info.height = 4;
    		info.minDepth = 0;
    		info.maxDepth = 1;

    		// Execute dynamic viewport command
    		final Command cmd = builder.setDynamicViewport(0, List.of(viewport));
    		cmd.record(lib, buffer);
    		verify(lib).vkCmdSetViewport(buffer, 0, 1, info);
    	}

		@DisplayName("A dynamic viewport must override the pipeline stage configuration")
    	@Test
    	void viewportInvalid() {
    		assertThrows(IllegalArgumentException.class, () -> builder.setDynamicViewport(0, List.of()));
    		assertThrows(IllegalArgumentException.class, () -> builder.setDynamicViewport(1, List.of(viewport)));
    	}

		@DisplayName("A scissor rectangle can be configured dynamically")
    	@Test
    	void scissor() {
    		// Init viewport and scissor
    		builder.viewport(viewport);
    		builder.scissor(rect);

    		// Init expected scissor rectangle
    		final var info = new VkRect2D() {
    			@Override
    			public boolean equals(Object obj) {
    				return dataEquals((VkRect2D) obj);
    			}
    		};
    		info.offset.x = 1;
    		info.offset.y = 2;
    		info.extent.width = 3;
    		info.extent.height = 4;

    		// Execute dynamic scissor command
    		final Command cmd = builder.setDynamicScissor(0, List.of(rect));
    		cmd.record(lib, buffer);
    		verify(lib).vkCmdSetScissor(buffer, 0, 1, info);
    	}

		@DisplayName("A dynamic scissor must override the pipeline stage configuration")
    	@Test
    	void scissorInvalid() {
    		assertThrows(IllegalArgumentException.class, () -> builder.setDynamicScissor(0, List.of()));
    		assertThrows(IllegalArgumentException.class, () -> builder.setDynamicScissor(1, List.of(rect)));
    	}
	}
}
