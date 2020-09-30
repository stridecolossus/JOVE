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
	void create() {
		// Build descriptor
		final var descriptor = builder
				// TODO
				//.binding(layout)
				.result();

		// Check descriptor
		assertNotNull(descriptor);
		assertEquals(0, descriptor.vertexBindingDescriptionCount);
		assertEquals(0, descriptor.vertexAttributeDescriptionCount);
		assertNotNull(descriptor.pVertexBindingDescriptions);
		assertNotNull(descriptor.pVertexAttributeDescriptions);
		assertEquals(0, descriptor.flags);
	}

	@Test
	void createEmpty() {
		final var descriptor = builder.result();
		assertNotNull(descriptor);
		assertEquals(0, descriptor.vertexBindingDescriptionCount);
		assertEquals(0, descriptor.vertexAttributeDescriptionCount);
		assertEquals(null, descriptor.pVertexBindingDescriptions);
		assertEquals(null, descriptor.pVertexAttributeDescriptions);
	}

	@Test
	void createDuplicateBindingIndex() {
		// TODO
	}

	@Test
	void createAttributeLocation() {
		// TODO
	}
}
