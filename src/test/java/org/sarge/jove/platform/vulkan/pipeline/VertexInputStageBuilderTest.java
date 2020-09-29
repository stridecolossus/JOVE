package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VertexInputStageBuilderTest {
	private VertexInputStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new VertexInputStageBuilder();
	}

	@Test
	void build() {
		// Build descriptor
		final var descriptor = builder
				// TODO
				//.binding(layout)
				.buildLocal();

		// Check descriptor
		assertNotNull(descriptor);
		assertEquals(0, descriptor.vertexBindingDescriptionCount);
		assertEquals(0, descriptor.vertexAttributeDescriptionCount);
		assertNotNull(descriptor.pVertexBindingDescriptions);
		assertNotNull(descriptor.pVertexAttributeDescriptions);
		assertEquals(0, descriptor.flags);
	}

	@Test
	void buildEmpty() {
		final var descriptor = builder.buildLocal();
		assertNotNull(descriptor);
		assertEquals(0, descriptor.vertexBindingDescriptionCount);
		assertEquals(0, descriptor.vertexAttributeDescriptionCount);
		assertEquals(null, descriptor.pVertexBindingDescriptions);
		assertEquals(null, descriptor.pVertexAttributeDescriptions);
	}

	@Test
	void buildDuplicateBindingIndex() {
		// TODO
	}

	@Test
	void buildAttributeLocation() {
		// TODO
	}
}
