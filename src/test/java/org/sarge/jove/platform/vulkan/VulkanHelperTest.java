package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VulkanHelper.FormatBuilder;

public class VulkanHelperTest {
	@Nested
	class ColourComponentTests {
		@Test
		public void colourComponent() {
			final int result = VulkanHelper.colourComponent("ARGB");
			final VkColorComponentFlag[] set = {
				VkColorComponentFlag.VK_COLOR_COMPONENT_R_BIT,
				VkColorComponentFlag.VK_COLOR_COMPONENT_G_BIT,
				VkColorComponentFlag.VK_COLOR_COMPONENT_B_BIT,
				VkColorComponentFlag.VK_COLOR_COMPONENT_A_BIT,
			};
			final int expected = IntegerEnumeration.mask(Set.of(set));
			assertEquals(expected, result);
			assertEquals(expected, VulkanHelper.DEFAULT_COLOUR_COMPONENT);
		}

		@Test
		public void colourComponentInvalidCharacter() {
			assertThrows(IllegalArgumentException.class, () -> VulkanHelper.colourComponent("cobblers"));
		}
	}

	@Nested
	class FormatBuilderTests {
		private FormatBuilder builder;

		@BeforeEach
		public void before() {
			builder = new FormatBuilder();
		}

		@Test
		public void build() {
			final VkFormat format = builder
				.type(Vertex.Component.Type.INT)
				.components("RGBA")
				.bytes(2)
				.signed(false)
				.build();
			assertEquals(VkFormat.VK_FORMAT_R16G16B16A16_UINT, format);
		}

		@Test
		public void buildDefaults() {
			assertEquals(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, builder.build());
		}

		@Test
		public void invalidComponents() {
			assertThrows(IllegalArgumentException.class, () -> builder.components(""));
			assertThrows(IllegalArgumentException.class, () -> builder.components("cobblers"));
			assertThrows(IllegalArgumentException.class, () -> builder.components("RGBA?"));
		}

		@Test
		public void invalidBytesPerComponent() {
			assertThrows(IllegalArgumentException.class, () -> builder.bytes(0));
			assertThrows(IllegalArgumentException.class, () -> builder.bytes(3));
		}

		@Test
		public void component() {
			assertEquals(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, VulkanHelper.format(Vertex.Component.COLOUR));
		}
	}
}
