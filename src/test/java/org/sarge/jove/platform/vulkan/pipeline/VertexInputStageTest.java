package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.platform.vulkan.*;

public class VertexInputStageTest {
	private static final VkFormat FORMAT = VkFormat.R32G32B32_SFLOAT;

	private VertexInputStage stage;

	@BeforeEach
	void before() {
		stage = new VertexInputStage();
	}

	@DisplayName("An empty vertex input stage has no vertex bindings or attributes")
	@Test
	void none() {
		final VkPipelineVertexInputStateCreateInfo info = stage.descriptor();
		assertEquals(0, info.flags);
		assertEquals(0, info.vertexBindingDescriptionCount);
		assertEquals(0, info.vertexAttributeDescriptionCount);
	}

	@DisplayName("The vertex input stage can be specified programatically")
	@Test
	void build() {
		final VkPipelineVertexInputStateCreateInfo info = stage
				.binding()
					.binding(1)
					.rate(VkVertexInputRate.VERTEX)
					.stride(4)
					.attribute()
						.location(2)
						.format(FORMAT)
						.offset(3)
						.build()
					.build()
				.descriptor();

		assertEquals(0, info.flags);
		assertEquals(1, info.vertexBindingDescriptionCount);
		assertEquals(1, info.vertexAttributeDescriptionCount);

		final VkVertexInputBindingDescription binding = info.pVertexBindingDescriptions[0];
		assertEquals(1, binding.binding);
		assertEquals(VkVertexInputRate.VERTEX, binding.inputRate);
		assertEquals(4, binding.stride);

		final VkVertexInputAttributeDescription attribute = info.pVertexAttributeDescriptions[0];
		assertEquals(1, attribute.binding);
		assertEquals(2, attribute.location);
		assertEquals(FORMAT, attribute.format);
		assertEquals(3, attribute.offset);
	}

	@DisplayName("The vertex bindings and attribute properties are initialised to sensible defaults if not specified")
	@Test
	void defaults() {
		final VkPipelineVertexInputStateCreateInfo info = stage
				.binding()
					.stride(1)
					.attribute(FORMAT)
				.build()
				.descriptor();

		assertEquals(0, info.flags);
		assertEquals(1, info.vertexBindingDescriptionCount);
		assertEquals(1, info.vertexAttributeDescriptionCount);

		final VkVertexInputBindingDescription binding = info.pVertexBindingDescriptions[0];
		assertEquals(0, binding.binding);
		assertEquals(VkVertexInputRate.VERTEX, binding.inputRate);
		assertEquals(1, binding.stride);

		final VkVertexInputAttributeDescription attribute = info.pVertexAttributeDescriptions[0];
		assertEquals(0, attribute.binding);
		assertEquals(0, attribute.location);
		assertEquals(FORMAT, attribute.format);
		assertEquals(0, attribute.offset);
	}

	@DisplayName("A vertex attribute can be derived from a component layout")
	@Test
	void layout() {
		final VkPipelineVertexInputStateCreateInfo info = stage
				.binding()
					.attribute(Point.LAYOUT)
				.build()
				.descriptor();

		assertEquals(0, info.flags);
		assertEquals(1, info.vertexBindingDescriptionCount);
		assertEquals(1, info.vertexAttributeDescriptionCount);

		final VkVertexInputBindingDescription binding = info.pVertexBindingDescriptions[0];
		assertEquals(3 * 4, binding.stride);

		final VkVertexInputAttributeDescription attribute = info.pVertexAttributeDescriptions[0];
		assertEquals(0, attribute.binding);
		assertEquals(0, attribute.location);
		assertEquals(FORMAT, attribute.format);
		assertEquals(0, attribute.offset);
	}

	@DisplayName("A vertex binding must contain at least one vertex attribute")
	@Test
	void empty() {
		assertThrows(IllegalStateException.class, () -> stage.binding().build().descriptor());
	}

	@DisplayName("Each vertex binding must have a unqiue binding index")
	@Test
	void duplicateBindingIndex() {
		stage
				.binding()
					.binding(1)
					.stride(2)
				.build();

		assertThrows(IllegalStateException.class, () -> stage.binding().binding(1));
	}

	@DisplayName("Each vertex attribute must have a unqiue location within its binding")
	@Test
	void duplicateAttributeLocation() {
		final var binding = stage
				.binding()
				.stride(2);

		binding
				.attribute()
				.location(2)
				.build();

		assertThrows(IllegalStateException.class, () -> binding.attribute().location(2).build());
	}

	@DisplayName("The offset of a vertex attribute cannot exceed the stride of its binding")
	@Test
	void invalidOffset() {
		final var binding = stage
				.binding()
				.stride(1);

		final var attribute = binding
				.attribute()
				.format(FORMAT)
				.offset(1);

		assertThrows(IndexOutOfBoundsException.class, () -> attribute.build());
	}
}
