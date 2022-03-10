package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Layout;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkVertexInputRate;

public class VertexInputPipelineStageBuilderTest {
	private static final VkFormat FORMAT = VkFormat.B8G8R8_SINT;
	private static final int STRIDE = 3 * Float.BYTES;

	private VertexInputPipelineStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new VertexInputPipelineStageBuilder();
	}

	@Test
	void buildEmpty() {
		final var descriptor = builder.get();
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
					.attribute()
						.location(3)
						.format(FORMAT)
						.offset(1)
						.build()
					.build()
				.get();

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
		assertEquals(VkVertexInputRate.VERTEX, binding.inputRate);

		// Check attribute
		final var attr = descriptor.pVertexAttributeDescriptions;
		assertNotNull(attr);
		assertEquals(1, attr.binding);
		assertEquals(3, attr.location);
		assertEquals(1, attr.offset);
		assertEquals(FORMAT, attr.format);
	}

	@Test
	void buildAddVertexLayout() {
		final var layout = List.of(Layout.floats(1), Layout.floats(2));
		final var descriptor = builder.add(layout).get();
		assertNotNull(descriptor);
		assertEquals(1, descriptor.vertexBindingDescriptionCount);
		assertEquals(2, descriptor.vertexAttributeDescriptionCount);
	}

	@Test
	void buildEmptyBinding() {
		assertThrows(IllegalArgumentException.class, "No attributes specified", () -> builder.binding().build());
	}

	@Test
	void bindingDuplicateIndex() {
		builder
				.binding()
				.index(0)
				.stride(STRIDE)
				.attribute()
					.format(FORMAT)
					.build()
				.build();

		assertThrows(IllegalArgumentException.class, "Duplicate binding index", () -> builder.binding().index(0).build());
	}

	@Test
	void attributeRequiresFormat() {
		final var binding = builder.binding().stride(STRIDE);
		assertThrows(IllegalArgumentException.class, "No format specified", () -> binding.attribute().build());
	}

	@Test
	void attributeInvalidOffset() {
		assertThrows(IllegalArgumentException.class, "Offset exceeds vertex stride", () -> builder.binding().attribute().offset(2).build());
	}

	@Test
	void attributeDuplicateLocation() {
		final var attribute = builder
				.binding()
					.stride(STRIDE)
					.attribute()
						.location(1)
						.format(FORMAT)
						.build()
					.attribute()
						.location(1)
						.format(FORMAT);

		assertThrows(IllegalArgumentException.class, "Duplicate location", () -> attribute.build());
	}
}
