package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.util.FormatBuilder.Type;

class FormatBuilderTest {
	private FormatBuilder builder;

	@BeforeEach
	void before() {
		builder = new FormatBuilder();
	}

	@Test
	void build() {
		builder
			.components("BGR")
			.bytes(1)
			.type(Type.RGB)
			.signed(true);
		assertEquals(VkFormat.B8G8R8_SRGB, builder.build());
	}

	@Test
	void buildDefaults() {
		assertEquals(VkFormat.R32G32B32A32_SFLOAT, builder.build());
	}

	@Test
	void buildComponentCount() {
		assertEquals(VkFormat.R32G32_SFLOAT, builder.count(2).build());
	}

	@Test
	void buildInvalidComponentCount() {
		assertThrows(IllegalArgumentException.class, () -> builder.components("RGB").count(4).build());
	}

	@Test
	void componentTemplateInvalid() {
		assertThrows(IllegalArgumentException.class, () -> builder.components(""));
		assertThrows(IllegalArgumentException.class, () -> builder.components("cobblers"));
		assertThrows(IllegalArgumentException.class, () -> builder.components("RGBA?"));
	}

	@Test
	void bytesInvalid() {
		assertThrows(IllegalArgumentException.class, () -> builder.bytes(0));
		assertThrows(IllegalArgumentException.class, () -> builder.bytes(3));
	}

	@Test
	void layout() {
		assertEquals(VkFormat.R16G16B16_SFLOAT, FormatBuilder.format(new Layout(3, Float.class, 2, true)));
		assertEquals(VkFormat.R8G8_UNORM, FormatBuilder.format(new Layout(2, Byte.class, 1, false)));
	}

	@Test
	void imageFormatHint() {
		final ImageData image = mock(ImageData.class);
		final VkFormat format = VkFormat.B8G8R8_SRGB;
		when(image.format()).thenReturn(format.value());
		assertEquals(format, FormatBuilder.format(image));
	}

	@Test
	void imageFormatLayout() {
		final ImageData image = mock(ImageData.class);
		when(image.layout()).thenReturn(Layout.floats(3));
		assertEquals(VkFormat.R32G32B32_SFLOAT, FormatBuilder.format(image));
	}

	@Nested
	class TypeTests {
		@Test
		void floats() {
			assertEquals(Type.FLOAT, Type.of(Float.class));
			assertEquals(Type.FLOAT, Type.of(float.class));
		}

		@Test
		void integer() {
			assertEquals(Type.INT, Type.of(Integer.class));
			assertEquals(Type.INT, Type.of(int.class));
			assertEquals(Type.INT, Type.of(Short.class));
			assertEquals(Type.INT, Type.of(short.class));
		}

		@Test
		void bytes() {
			assertEquals(Type.NORM, Type.of(Byte.class));
			assertEquals(Type.NORM, Type.of(byte.class));
		}

		@Test
		void invalidComponentTypeMapping() {
			assertThrows(IllegalArgumentException.class, () -> Type.of(String.class));
		}
	}
}
