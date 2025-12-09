package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.pipeline.VertexInputStage.*;

public class VertexInputStageTest {
	private static final VkFormat FORMAT = VkFormat.R32G32B32_SFLOAT;

	private VertexInputStage stage;

	@BeforeEach
	void before() {
		stage = new VertexInputStage();
	}

	@DisplayName("A vertex binding can be constructed using a builder")
	@Test
	void build() {
		final var attribute = new VertexAttribute(2, FORMAT, 3);

		final var binding = new VertexBinding.Builder()
				.index(1)
				.attribute(attribute)
				.stride(4)
				.build();

		final var expected = new VertexBinding(1, 4, VkVertexInputRate.VERTEX, List.of(attribute));
		assertEquals(expected, binding);
	}

	@DisplayName("The pipeline stage generates the descriptor for the vertex bindings and attributes")
	@Test
	void descriptor() {
		// Build a binding
		final var binding = new VertexBinding.Builder()
				.index(1)
				.stride(12)
				.attribute(new VertexAttribute(2, FORMAT, 0))
				.build();

		// Add to pipeline stage
		stage.add(binding);

		// Check descriptor
		final VkPipelineVertexInputStateCreateInfo descriptor = stage.descriptor();
		assertEquals(0, descriptor.flags);
		assertEquals(1, descriptor.vertexBindingDescriptionCount);
		assertEquals(1, descriptor.pVertexBindingDescriptions.length);
		assertEquals(1, descriptor.vertexAttributeDescriptionCount);
		assertEquals(1, descriptor.pVertexAttributeDescriptions.length);

		// Check binding descriptor
		final VkVertexInputBindingDescription description = descriptor.pVertexBindingDescriptions[0];
		assertEquals(1, description.binding);
		assertEquals(VkVertexInputRate.VERTEX, description.inputRate);
		assertEquals(12, description.stride);

		// Check attribute descriptor
		final VkVertexInputAttributeDescription attribute = descriptor.pVertexAttributeDescriptions[0];
		assertEquals(2, attribute.location);
		assertEquals(FORMAT, attribute.format);
		assertEquals(0, attribute.offset);
	}

	@DisplayName("A vertex binding can be generated from a vertex layout")
	@Test
	void layout() {
		final var layout = Layout.floats(3);
		final var binding = VertexBinding.of(1, 2, List.of(layout, layout));
		final var one = new VertexAttribute(2, FORMAT, 0);
		final var two = new VertexAttribute(3, FORMAT, 3 * 4);
		final var expected = new VertexBinding(1, 2 * 3 * 4, VkVertexInputRate.VERTEX, List.of(one, two));
		assertEquals(expected, binding);
	}

	@DisplayName("The index of a vertex binding must be unique")
	@Test
	void duplicateBindingIndex() {
		final var binding = new VertexBinding.Builder()
				.index(2)
				.stride(3)
				.build();

		stage.add(binding);
		assertThrows(IllegalArgumentException.class, () -> stage.add(binding));
	}

	@DisplayName("The location of a vertex attribute must be unique")
	@Test
	void duplicateAttributeLocation() {
		final var binding = new VertexBinding.Builder()
				.attribute(new VertexAttribute(3, FORMAT, 0))
				.attribute(new VertexAttribute(3, FORMAT, 0))
				.stride(2)
				.build();

		assertThrows(IllegalArgumentException.class, () -> stage.add(binding));
	}

	@DisplayName("The location of a vertex attribute must be unique across all bindings in the pipeline")
	@Test
	void duplicateAttributeLocationAcrossBindings() {
		final var one = new VertexBinding.Builder()
				.attribute(new VertexAttribute(3, FORMAT, 0))
				.stride(1)
				.build();

		final var two = new VertexBinding.Builder()
				.attribute(new VertexAttribute(3, FORMAT, 0))
				.stride(1)
				.build();

		stage.add(one);
		assertThrows(IllegalArgumentException.class, () -> stage.add(two));
	}

	@DisplayName("The stride of a vertex binding must be larger than the offset of all its attributes")
	@Test
	void invalidBindingStride() {
		final var builder = new VertexBinding.Builder()
				.stride(2)
				.attribute(new VertexAttribute(0, FORMAT, 3));

		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}
}
