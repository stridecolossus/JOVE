package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;

public class VertexInputStageBuilderTest {
	private static final VkFormat FORMAT = VkFormat.B8G8R8_SINT;
	private static final int STRIDE = 3 * Float.BYTES;

	private VertexInputStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new VertexInputStageBuilder();
	}

	@DisplayName("The vertex configuration can be empty")
	@Test
	void empty() {
		final var descriptor = builder.get();
		assertEquals(0, descriptor.vertexBindingDescriptionCount);
		assertEquals(0, descriptor.vertexAttributeDescriptionCount);
		assertEquals(null, descriptor.pVertexBindingDescriptions);
		assertEquals(null, descriptor.pVertexAttributeDescriptions);
	}

	@DisplayName("The vertex configuration can be explicitly constructed by a builder")
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
		assertEquals(1, descriptor.vertexBindingDescriptionCount);
		assertEquals(1, descriptor.vertexAttributeDescriptionCount);

		// Check binding
		final var binding = descriptor.pVertexBindingDescriptions;
		assertEquals(1, binding.binding);
		assertEquals(2, binding.stride);
		assertEquals(VkVertexInputRate.VERTEX, binding.inputRate);

		// Check attribute
		final var attr = descriptor.pVertexAttributeDescriptions;
		assertEquals(1, attr.binding);
		assertEquals(3, attr.location);
		assertEquals(1, attr.offset);
		assertEquals(FORMAT, attr.format);
	}

	@DisplayName("The vertex configuration can be specified by a vertex layout")
	@Test
	void layout() {
		final var layout = new CompoundLayout(Layout.floats(1), Layout.floats(2));
		final var descriptor = builder.add(layout).get();
		assertEquals(1, descriptor.vertexBindingDescriptionCount);
		assertEquals(2, descriptor.vertexAttributeDescriptionCount);
	}

	@DisplayName("A vertex binding...")
	@Nested
	class BindingTests {
		@DisplayName("cannot duplicate the index of another binding")
		@Test
		void index() {
			builder
					.binding()
					.index(0)
					.stride(STRIDE)
					.attribute()
						.format(FORMAT)
						.build()
					.build();

			assertThrows(IllegalArgumentException.class, () -> builder.binding().index(0).build());
		}

		@DisplayName("implies at least one vertex attribute")
		@Test
		void bindings() {
			assertThrows(IllegalArgumentException.class, () -> builder.binding().build());
		}
	}

	@DisplayName("A vertex attribute...")
	@Nested
	class AttributeTests {
		@DisplayName("must have a specified format")
		@Test
		void format() {
			final var binding = builder.binding().stride(STRIDE);
			assertThrows(IllegalArgumentException.class, () -> binding.attribute().build());
		}

		@DisplayName("cannot specify an offset larger than the vertex stride")
		@Test
		void offset() {
			assertThrows(IllegalArgumentException.class, () -> builder.binding().attribute().offset(2).build());
		}

		@DisplayName("cannot duplicate the location of another attribute")
		@Test
		void location() {
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

			assertThrows(IllegalArgumentException.class, () -> attribute.build());
		}
	}
}
