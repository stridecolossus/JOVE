package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkComponentMapping;
import org.sarge.jove.platform.vulkan.VkComponentSwizzle;

public class ComponentMappingBuilderTest {
	@Test
	void identity() {
		assertEquals(VkComponentSwizzle.IDENTITY, ComponentMappingBuilder.IDENTITY.r);
		assertEquals(VkComponentSwizzle.IDENTITY, ComponentMappingBuilder.IDENTITY.g);
		assertEquals(VkComponentSwizzle.IDENTITY, ComponentMappingBuilder.IDENTITY.b);
		assertEquals(VkComponentSwizzle.IDENTITY, ComponentMappingBuilder.IDENTITY.a);
	}

	@Test
	void map() {
		final VkComponentMapping mapping = ComponentMappingBuilder.build("ABGR");
		assertNotNull(mapping);
		assertEquals(VkComponentSwizzle.A, mapping.r);
		assertEquals(VkComponentSwizzle.B, mapping.g);
		assertEquals(VkComponentSwizzle.G, mapping.b);
		assertEquals(VkComponentSwizzle.R, mapping.a);
	}

	@Test
	void special() {
		final VkComponentMapping mapping = ComponentMappingBuilder.build("10==");
		assertNotNull(mapping);
		assertEquals(VkComponentSwizzle.ONE, mapping.r);
		assertEquals(VkComponentSwizzle.ZERO, mapping.g);
		assertEquals(VkComponentSwizzle.IDENTITY, mapping.b);
		assertEquals(VkComponentSwizzle.IDENTITY, mapping.a);
	}

	@Test
	void mapInvalidLength() {
		assertThrows(IllegalArgumentException.class, () -> ComponentMappingBuilder.build("RGB"));
	}

	@Test
	void mapInvalidSwizzle() {
		assertThrows(IllegalArgumentException.class, () -> ComponentMappingBuilder.build("XXXX"));
	}
}
