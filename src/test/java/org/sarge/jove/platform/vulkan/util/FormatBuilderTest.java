package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.ImageData;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.util.FormatBuilder.Type;

public class FormatBuilderTest {
	private FormatBuilder builder;

	@BeforeEach
	void before() {
		builder = new FormatBuilder();
	}

	@Test
	void build() {
		final VkFormat format = builder
			.type(Type.INTEGER)
			.components("RGBA")
			.bytes(2)
			.signed(false)
			.build();
		assertEquals(VkFormat.VK_FORMAT_R16G16B16A16_UINT, format);
	}

	@Test
	void buildDefaults() {
		assertEquals(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, builder.build());
	}

	@Test
	void buildComponents() {
		assertEquals(VkFormat.VK_FORMAT_R32G32_SFLOAT, builder.components(2).build());
	}

	@Test
	void invalidComponentString() {
		assertThrows(IllegalArgumentException.class, () -> builder.components(""));
		assertThrows(IllegalArgumentException.class, () -> builder.components("cobblers"));
		assertThrows(IllegalArgumentException.class, () -> builder.components("RGBA?"));
	}

	@Test
	void invalidBytesPerComponent() {
		assertThrows(IllegalArgumentException.class, () -> builder.bytes(0));
		assertThrows(IllegalArgumentException.class, () -> builder.bytes(3));
	}

	@Test
	void invalidComponentNumber() {
		assertThrows(IllegalArgumentException.class, () -> builder.components("RGB").components(4).build());
	}

	@Test
	void imageFormat() {
		final ImageData image = mock(ImageData.class);
		when(image.components()).thenReturn(List.of(8, 8, 8, 8));
		assertEquals(VkFormat.VK_FORMAT_R8G8B8A8_UNORM, FormatBuilder.format(image));
	}
}
