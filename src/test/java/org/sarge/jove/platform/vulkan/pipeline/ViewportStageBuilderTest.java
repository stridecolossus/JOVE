package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;

public class ViewportStageBuilderTest {
	private ViewportStageBuilder builder;
	private Rectangle rect;

	@BeforeEach
	void before() {
		builder = new ViewportStageBuilder();
		rect = new Rectangle(new Dimensions(3, 4));
	}

	@Test
	void build() {
		// Build descriptor
		final var descriptor = builder
				.viewport(rect)
				.scissor(rect)
				.buildLocal();

		// Check descriptor
		assertNotNull(descriptor);
		assertEquals(1, descriptor.viewportCount);
		assertEquals(1, descriptor.scissorCount);
		assertNotNull(descriptor.pViewports);
		assertNotNull(descriptor.pScissors);
		assertEquals(0, descriptor.flags);
	}

	@Test
	void buildRequiresViewport() {
		assertThrows(IllegalArgumentException.class, () -> builder.buildLocal());
	}

	@Test
	void buildRequiresScissor() {
		builder.viewport(rect);
		assertThrows(IllegalArgumentException.class, () -> builder.buildLocal());
	}
}
