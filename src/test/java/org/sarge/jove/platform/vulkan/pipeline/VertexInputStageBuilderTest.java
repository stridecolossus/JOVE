package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkVertexInputRate;

public class VertexInputStageBuilderTest {
	private static final VkFormat FORMAT = VkFormat.VK_FORMAT_B8G8R8_SINT;

	private VertexInputStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new VertexInputStageBuilder();
	}

	private void addBinding() {
		builder.binding().index(0).stride(2).build();
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
					.index(1)
					.stride(2)
					.build()
				.attribute()
					.binding(1)
					.format(FORMAT)
					.offset(1)
					.build()
				.result();

		// Check descriptor
		assertNotNull(descriptor);
		assertEquals(0, descriptor.flags);
		assertEquals(1, descriptor.vertexBindingDescriptionCount);
		assertEquals(1, descriptor.vertexAttributeDescriptionCount);

		// Check binding
		final var binding = descriptor.pVertexBindingDescriptions;
		assertNotNull(binding);
		assertEquals(1, binding.binding);
		assertEquals(2, binding.stride);
		assertEquals(VkVertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX, binding.inputRate);

		// Check attribute
		final var attr = descriptor.pVertexAttributeDescriptions;
		assertNotNull(attr);
		assertEquals(1, attr.binding);
		assertEquals(0, attr.location);
		assertEquals(1, attr.offset);
		assertEquals(FORMAT, attr.format);
	}

	@Test
	void buildBindingFromVertexLayout() {
		final var layout = new Vertex.Layout(List.of(Vertex.Component.POSITION, Vertex.Component.COLOUR));
		final var descriptor = builder.binding(layout).result();
		assertNotNull(descriptor);
		assertEquals(1, descriptor.vertexBindingDescriptionCount);
		assertEquals(2, descriptor.vertexAttributeDescriptionCount);
	}

	@Test
	void buildEmptyBinding() {
		addBinding();
		assertThrows(IllegalArgumentException.class, "No attributes specified", () -> builder.result());
	}

	// TODO - how to test the descriptors for bindings/attributes?

	@Test
	void bindingInvalidVertexStride() {
		assertThrows(IllegalArgumentException.class, "Invalid vertex stride", () -> builder.binding().build());
	}

	@Test
	void bindingDuplicateIndex() {
		addBinding();
		assertThrows(IllegalArgumentException.class, "Duplicate binding index", () -> builder.binding().index(0).build());
	}

	@Test
	void attributeRequiresFormat() {
		addBinding();
		assertThrows(IllegalArgumentException.class, "No format specified", () -> builder.attribute().binding(0).build());
	}

	@Test
	void attributeInvalidBindingIndex() {
		addBinding();
		assertThrows(IllegalArgumentException.class, "Invalid binding index", () -> builder.attribute().binding(1).build());
	}

	@Test
	void attributeInvalidOffset() {
		addBinding();
		assertThrows(IllegalArgumentException.class, "Offset exceeds vertex stride", () -> builder.attribute().binding(0).offset(2).build());
	}

	@Test
	void attributeDuplicateLocation() {
		addBinding();
		builder.attribute().format(FORMAT).build();
		assertThrows(IllegalArgumentException.class, "Duplicate location", () -> builder.attribute().format(FORMAT).build());
	}
}
