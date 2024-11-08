package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;

class ComponentMappingTest {
	@DisplayName("The identity component mapping is comprised of the identity swizzle")
	@Test
	void identity() {
		final VkComponentMapping mapping = ComponentMapping.IDENTITY.build();
		assertEquals(VkComponentSwizzle.IDENTITY, mapping.r);
		assertEquals(VkComponentSwizzle.IDENTITY, mapping.g);
		assertEquals(VkComponentSwizzle.IDENTITY, mapping.b);
		assertEquals(VkComponentSwizzle.IDENTITY, mapping.a);
	}

	@DisplayName("A component mapping can be constructed from the components of an image")
	@Test
	void of() {
		final VkComponentMapping mapping = ComponentMapping.of("RGBA").build();
		assertEquals(VkComponentSwizzle.R, mapping.r);
		assertEquals(VkComponentSwizzle.G, mapping.g);
		assertEquals(VkComponentSwizzle.B, mapping.b);
		assertEquals(VkComponentSwizzle.A, mapping.a);
	}

	@DisplayName("Unspecified channels are IDENTITY by default")
	@Test
	void unspecified() {
		final VkComponentMapping mapping = ComponentMapping.of("R").build();
		assertEquals(VkComponentSwizzle.R, mapping.r);
		assertEquals(VkComponentSwizzle.IDENTITY, mapping.g);
		assertEquals(VkComponentSwizzle.IDENTITY, mapping.b);
		assertEquals(VkComponentSwizzle.IDENTITY, mapping.a);
	}

	@DisplayName("A component mapping also supports the special case swizzle characters")
	@Test
	void special() {
		final VkComponentMapping mapping = ComponentMapping.of("10=R").build();
		assertEquals(VkComponentSwizzle.ONE, mapping.r);
		assertEquals(VkComponentSwizzle.ZERO, mapping.g);
		assertEquals(VkComponentSwizzle.IDENTITY, mapping.b);
		assertEquals(VkComponentSwizzle.R, mapping.a);
	}

	@DisplayName("A component mapping string cannot be empty")
	@Test
	void empty() {
		assertThrows(IllegalArgumentException.class, () -> ComponentMapping.of(""));
	}

	@DisplayName("A component mapping string cannot be longer than 4 characters")
	@Test
	void length() {
		assertThrows(IllegalArgumentException.class, () -> ComponentMapping.of("12345"));
	}

	@DisplayName("A component mapping must be comprised of supported swizzles")
	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> ComponentMapping.of("?"));
	}
}
