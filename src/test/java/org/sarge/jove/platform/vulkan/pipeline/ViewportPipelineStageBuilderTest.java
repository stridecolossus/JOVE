package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.pipeline.ViewportPipelineStageBuilder.Viewport;

public class ViewportPipelineStageBuilderTest {
	private ViewportPipelineStageBuilder builder;
	private Rectangle rect;

	@BeforeEach
	void before() {
		builder = new ViewportPipelineStageBuilder();
		rect = new Rectangle(1, 2, 3, 4);
	}

	@Test
	void build() {
		// Build descriptor
		final var descriptor = builder
				.viewport(new Viewport(rect))
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

	@DisplayName("The viewport coordinate system can be globally flipped vertically")
	@Test
	void flip() {
		final var descriptor = builder
				.flip(true)
				.viewport(new Viewport(rect))
				.scissor(rect)
				.get();

		assertEquals(1, descriptor.pViewports.x);
		assertEquals(2 + 4, descriptor.pViewports.y);
		assertEquals(3, descriptor.pViewports.width);
		assertEquals(-4, descriptor.pViewports.height);
	}

	@DisplayName("The viewport configuration cannot be empty")
	@Test
	void empty() {
		assertThrows(IllegalArgumentException.class, () -> builder.get());
	}

	@DisplayName("The number of scissor rectangles must match the number of viewports")
	@Test
	void scissor() {
		builder.viewport(new Viewport(rect));
		assertThrows(IllegalArgumentException.class, () -> builder.get());
	}
}
