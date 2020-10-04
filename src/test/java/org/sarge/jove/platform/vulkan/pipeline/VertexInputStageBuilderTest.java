package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.platform.vulkan.VkFormat;

public class VertexInputStageBuilderTest {
	private VertexInputStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new VertexInputStageBuilder();
	}

	@Test
	void buildEmpty() {
		final var descriptor = builder.result();
		assertNotNull(descriptor);
		assertEquals(0, descriptor.vertexBindingDescriptionCount);
		assertEquals(0, descriptor.vertexAttributeDescriptionCount);
		assertEquals(null, descriptor.pVertexBindingDescriptions);
		assertEquals(null, descriptor.pVertexAttributeDescriptions);
	}

	@Test
	void build() {
		// Build descriptor
		final var descriptor = builder
				.binding()
					.binding(1)
					.stride(2)
					.build()
				.attribute()
					.binding(1)
					.format(VkFormat.VK_FORMAT_A1R5G5B5_UNORM_PACK16)
					.offset(1)
					.build()
				.result();

		// Check descriptor
		assertNotNull(descriptor);
		assertEquals(1, descriptor.vertexBindingDescriptionCount);
		assertEquals(1, descriptor.vertexAttributeDescriptionCount);
		assertNotNull(descriptor.pVertexBindingDescriptions);
		assertNotNull(descriptor.pVertexAttributeDescriptions);
		assertEquals(0, descriptor.flags);
	}

	@Test
	void buildLayout() {
		final var layout = new Vertex.Layout(List.of(Vertex.Component.POSITION, Vertex.Component.COLOUR));
		final var descriptor = builder.binding(layout).result();
		assertNotNull(descriptor);
		assertEquals(1, descriptor.vertexBindingDescriptionCount);
		assertEquals(2, descriptor.vertexAttributeDescriptionCount);
	}

	// TODO - how to test the descriptors for bindings/attributes?

	private void addBinding() {
		builder.binding().binding(0).stride(2).build();
	}

	@Test
	void bindingInvalidVertexStride() {
		assertThrows(IllegalArgumentException.class, () -> builder.binding().build());
	}

	@Test
	void bindingDuplicateIndex() {
		addBinding();
		assertThrows(IllegalArgumentException.class, () -> builder.binding().binding(0).build());
	}

	@Test
	void attributeRequiresFormat() {
		addBinding();
		assertThrows(IllegalArgumentException.class, () -> builder.attribute().binding(0).build());
	}

	@Test
	void attributeInvalidBindingIndex() {
		addBinding();
		assertThrows(IllegalArgumentException.class, () -> builder.attribute().binding(1).build());
	}

	@Test
	void attributeInvalidOffset() {
		addBinding();
		assertThrows(IllegalArgumentException.class, () -> builder.attribute().binding(0).offset(2).build());
	}
}
