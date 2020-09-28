package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.IntegerEnumeration;

public class VulkanHelperTest {
	@Nested
	class Utilities {
		@Test
		public void topology() {
			assertEquals(VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_POINT_LIST, VulkanHelper.topology(Primitive.POINT_LIST));
			assertEquals(VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_LINE_LIST, VulkanHelper.topology(Primitive.LINE_LIST));
			assertEquals(VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP, VulkanHelper.topology(Primitive.LINE_STRIP));
			assertEquals(VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST, VulkanHelper.topology(Primitive.TRIANGLE_LIST));
			assertEquals(VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP, VulkanHelper.topology(Primitive.TRIANGLE_STRIP));
			assertEquals(VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_FAN, VulkanHelper.topology(Primitive.TRIANGLE_FAN));
		}
	}

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
}
