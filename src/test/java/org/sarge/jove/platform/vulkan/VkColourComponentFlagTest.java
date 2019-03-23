package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.IntegerEnumeration;

public class VkColourComponentFlagTest {
	@Test
	public void mask() {
		final int result = VkColorComponentFlag.mask("ARGB");
		final VkColorComponentFlag[] set = {
			VkColorComponentFlag.VK_COLOR_COMPONENT_R_BIT,
			VkColorComponentFlag.VK_COLOR_COMPONENT_G_BIT,
			VkColorComponentFlag.VK_COLOR_COMPONENT_B_BIT,
			VkColorComponentFlag.VK_COLOR_COMPONENT_A_BIT,
		};
		final int expected = IntegerEnumeration.mask(Set.of(set));
		assertEquals(expected, result);
		assertEquals(expected, VkColorComponentFlag.DEFAULT);
	}

	@Test
	public void ofInvalidCharacter() {
		assertThrows(IllegalArgumentException.class, () -> VkColorComponentFlag.mask("cobblers"));
	}
}
