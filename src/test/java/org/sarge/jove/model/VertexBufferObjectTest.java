package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Vertex.Component;
import org.sarge.jove.model.VertexBufferObject.Layout;
import org.sarge.jove.model.VertexBufferObject.Layout.Attribute;
import org.sarge.jove.model.VertexBufferObject.Layout.Builder;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkVertexInputRate;

public class VertexBufferObjectTest {
	@Nested
	class AttributeTests {
		@Test
		public void constructor() {
			final Attribute attr = new Attribute(0, Component.POSITION, 1);
			assertEquals(0, attr.location());
			assertEquals(Component.POSITION, attr.component());
			assertEquals(1, attr.offset());
			assertEquals(VkFormat.VK_FORMAT_R32G32B32_SFLOAT, attr.format());
		}
	}

	@Nested
	class LayoutTests {
		private Attribute attr;

		@BeforeEach
		public void before() {
			attr = new Attribute(0, Component.POSITION, 1);
		}

		@Test
		public void constructor() {
			final Layout layout = new Layout(2, VkVertexInputRate.VK_VERTEX_INPUT_RATE_INSTANCE, List.of(attr), 3);
			assertEquals(2, layout.binding());
			assertEquals(VkVertexInputRate.VK_VERTEX_INPUT_RATE_INSTANCE, layout.rate());
			assertEquals(List.of(attr), layout.layout());
			assertEquals(3, layout.stride());
		}

		@Test
		public void emptyAttributes() {
			assertThrows(IllegalArgumentException.class, () -> new Layout(2, VkVertexInputRate.VK_VERTEX_INPUT_RATE_INSTANCE, List.of(), 3));
		}

		@Test
		public void duplicateAttributeLocation() {
			assertThrows(IllegalArgumentException.class, () -> new Layout(2, VkVertexInputRate.VK_VERTEX_INPUT_RATE_INSTANCE, List.of(attr, attr), 3));
		}
	}

	@Nested
	class BuilderTests {
		private Builder builder;

		@BeforeEach
		public void before() {
			builder = new Builder();
		}

		@Test
		public void build() {
			// Build layout
			final Layout layout = builder
				.binding(1)
				.rate(VkVertexInputRate.VK_VERTEX_INPUT_RATE_INSTANCE)
				.add(2, Component.POSITION)
				.add(Component.COLOUR)
				.build();

			// Check layout
			assertNotNull(layout);
			assertEquals(1, layout.binding());
			assertEquals(VkVertexInputRate.VK_VERTEX_INPUT_RATE_INSTANCE, layout.rate());
			assertNotNull(layout.layout());
			assertEquals(2, layout.layout().size());
			assertEquals((3 + 4) * Float.BYTES, layout.stride());

			// Check vertex position attribute
			final Attribute pos = layout.layout().get(0);
			assertEquals(2, pos.location());
			assertEquals(Component.POSITION, pos.component());
			assertEquals(0, pos.offset());
			assertEquals(VkFormat.VK_FORMAT_R32G32B32_SFLOAT, pos.format());

			// Check vertex colour attribute
			final Attribute col = layout.layout().get(1);
			assertEquals(3, col.location());
			assertEquals(Component.COLOUR, col.component());
			assertEquals(3 * Float.BYTES, col.offset());
			assertEquals(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, col.format());
		}
	}
}
