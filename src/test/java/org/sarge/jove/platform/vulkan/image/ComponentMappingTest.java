package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkComponentMapping;
import org.sarge.jove.platform.vulkan.VkComponentSwizzle;

class ComponentMappingTest {
	@Test
	void identity() {
		final VkComponentMapping mapping = ComponentMapping.IDENTITY.build();
		assertEquals(VkComponentSwizzle.IDENTITY, mapping.r);
		assertEquals(VkComponentSwizzle.IDENTITY, mapping.g);
		assertEquals(VkComponentSwizzle.IDENTITY, mapping.b);
		assertEquals(VkComponentSwizzle.IDENTITY, mapping.a);
	}

	@Test
	void map() {
		final ComponentMapping mapping = new ComponentMapping("ABGR");
		final VkComponentMapping descriptor = mapping.build();
		assertNotNull(descriptor);
		assertEquals(VkComponentSwizzle.A, descriptor.r);
		assertEquals(VkComponentSwizzle.B, descriptor.g);
		assertEquals(VkComponentSwizzle.G, descriptor.b);
		assertEquals(VkComponentSwizzle.R, descriptor.a);
	}

	@Test
	void special() {
		final ComponentMapping mapping = new ComponentMapping("10==");
		final VkComponentMapping descriptor = mapping.build();
		assertNotNull(descriptor);
		assertEquals(VkComponentSwizzle.ONE, descriptor.r);
		assertEquals(VkComponentSwizzle.ZERO, descriptor.g);
		assertEquals(VkComponentSwizzle.IDENTITY, descriptor.b);
		assertEquals(VkComponentSwizzle.IDENTITY, descriptor.a);
	}

	@Test
	void mapEmptyMapping() {
		assertThrows(IllegalArgumentException.class, () -> new ComponentMapping(StringUtils.EMPTY));
	}

	@Test
	void mapInvalidLength() {
		assertThrows(IllegalArgumentException.class, () -> new ComponentMapping("12345"));
	}

	@Test
	void mapInvalidSwizzle() {
		assertThrows(IllegalArgumentException.class, () -> new ComponentMapping("?"));
	}
}
