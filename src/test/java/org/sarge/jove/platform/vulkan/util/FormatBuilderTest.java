package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.util.FormatBuilder.Type;

public class FormatBuilderTest {
	private FormatBuilder builder;

	@BeforeEach
	public void before() {
		builder = new FormatBuilder();
	}

	@Test
	public void build() {
		final VkFormat format = builder
			.type(Type.INTEGER)
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
	public void buildComponents() {
		assertEquals(VkFormat.VK_FORMAT_R32G32_SFLOAT, builder.components(2).build());
	}

	@Test
	public void invalidComponentString() {
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
	public void invalidComponentNumber() {
		assertThrows(IllegalArgumentException.class, () -> builder.components("RGB").components(4).build());
	}
}
