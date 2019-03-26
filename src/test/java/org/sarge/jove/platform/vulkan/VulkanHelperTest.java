package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VulkanHelper.FormatBuilder;

public class VulkanHelperTest {
	@Nested
	class ColourComponentTests {
		@Test
		public void mask() {
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
		public void ofInvalidCharacter() {
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
				.components(2)
				.size(16)
				.signed(false)
				.type(FormatBuilder.Type.INT)
				.build();
			assertEquals(VkFormat.VK_FORMAT_R16G16_UINT, format);
		}

		@Test
		public void buildDefaults() {
			assertEquals(VkFormat.VK_FORMAT_R32G32B32_SFLOAT, builder.build());
		}

		@Test
		public void invalidComponents() {
			assertThrows(IllegalArgumentException.class, () -> builder.components(999));
		}

		@Test
		public void invalidComponentSize() {
			assertThrows(IllegalArgumentException.class, () -> builder.size(24));
		}
	}
}
