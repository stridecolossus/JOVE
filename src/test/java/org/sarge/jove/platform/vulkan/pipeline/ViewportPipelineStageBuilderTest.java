package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Rectangle;
import org.sarge.lib.util.Percentile;

public class ViewportPipelineStageBuilderTest {
	private ViewportPipelineStageBuilder builder;
	private Rectangle rect;

	@BeforeEach
	void before() {
		builder = new ViewportPipelineStageBuilder(null);
		rect = new Rectangle(1, 2, 3, 4);
	}

	@Test
	void build() {
		// Build descriptor
		final var descriptor = builder
				.viewport(rect, new Percentile(0.1f), new Percentile(0.2f))
				.scissor(rect)
				.get();

		// Check descriptor
		assertNotNull(descriptor);
		assertEquals(0, descriptor.flags);
		assertEquals(1, descriptor.viewportCount);
		assertEquals(1, descriptor.scissorCount);

		// Check viewport
		assertNotNull(descriptor.pViewports);
		assertEquals(1, descriptor.pViewports.x);
		assertEquals(2, descriptor.pViewports.y);
		assertEquals(3, descriptor.pViewports.width);
		assertEquals(4, descriptor.pViewports.height);
		assertEquals(0.1f, descriptor.pViewports.minDepth);
		assertEquals(0.2f, descriptor.pViewports.maxDepth);

		// Check scissor
		assertNotNull(descriptor.pScissors);
		assertEquals(1, descriptor.pScissors.offset.x);
		assertEquals(2, descriptor.pScissors.offset.y);
		assertEquals(3, descriptor.pScissors.extent.width);
		assertEquals(4, descriptor.pScissors.extent.height);
	}

	@Test
	void viewportAndScissor() {
		final var descriptor = builder.viewport(rect, true).get();
		assertEquals(1, descriptor.scissorCount);
	}

	@Test
	void flip() {
		final var descriptor = builder
				.flip(true)
				.viewport(rect)
				.scissor(rect)
				.get();
		assertEquals(1, descriptor.pViewports.x);
		assertEquals(2 + 4, descriptor.pViewports.y);
		assertEquals(3, descriptor.pViewports.width);
		assertEquals(-4, descriptor.pViewports.height);
	}

	@Test
	void createRequiresViewport() {
		assertThrows(IllegalArgumentException.class, () -> builder.get());
	}

	@Test
	void createRequiresScissor() {
		builder.viewport(rect);
		assertThrows(IllegalArgumentException.class, () -> builder.get());
	}
}
