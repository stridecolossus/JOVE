package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VertexInputStageBuilderTest {
	private VertexInputStageBuilder<?> builder;

	@BeforeEach
	void before() {
		builder = new VertexInputStageBuilder<>();
	}

	@Test
	void build() {
		// Build stage descriptor
		final var info = builder
//				.binding(xxx)
				.buildLocal();

		// Check descriptor
		assertNotNull(info);
		assertEquals(0, info.flags);

		// Check bindings
		assertEquals(1, info.vertexBindingDescriptionCount);
		assertNotNull(info.pVertexBindingDescriptions);

		// Check attributes
		// TODO
	}

	@Test
	void buildEmpty() {
		final var info = builder.buildLocal();
		assertNotNull(info);
		assertEquals(0, info.vertexBindingDescriptionCount);
		assertEquals(null, info.pVertexBindingDescriptions);
		assertEquals(0, info.vertexAttributeDescriptionCount);
		assertEquals(null, info.pVertexAttributeDescriptions);
	}

	@Test
	void buildDuplicateBindingLocation() {
		assertThrows(IllegalArgumentException.class, () -> builder.buildLocal());
	}
}
