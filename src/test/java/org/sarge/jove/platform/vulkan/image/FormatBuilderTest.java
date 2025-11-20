package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.image.FormatBuilder.NumericFormat;
import org.sarge.jove.util.ImageData;

class FormatBuilderTest {
	private FormatBuilder builder;

	@BeforeEach
	void before() {
		builder = new FormatBuilder();
	}

	@DisplayName("The builder can construct a valid format")
	@Test
	void build() {
		builder
				.components("RGBA")
				.count(4)
				.bytes(4)
				.signed(true)
				.type(NumericFormat.FLOAT);

		assertEquals(VkFormat.R32G32B32A32_SFLOAT, builder.build());
	}

	@DisplayName("The builder is initialised to default values")
	@Test
	void def() {
		assertEquals(VkFormat.R32G32B32A32_SFLOAT, builder.build());
	}

	@DisplayName("The builder can construct a signed RGB surface format")
	@Test
	void srgb() {
		builder
			.components("BGR")
			.bytes(1)
			.type(NumericFormat.RGB)
			.signed(true);

		assertEquals(VkFormat.B8G8R8_SRGB, builder.build());
	}

	@DisplayName("The component count cannot be longer than the configured components string")
	@Test
	void count() {
		assertThrows(IllegalArgumentException.class, () -> builder.count(5).build());
		assertThrows(IllegalArgumentException.class, () -> builder.components("RGB").count(4).build());
	}

	@DisplayName("The components string cannot be longer than 4 characters")
	@Test
	void components() {
		assertThrows(IllegalArgumentException.class, () -> builder.components("cobblers"));
	}

	@DisplayName("The components string cannot be empty")
	@Test
	void empty() {
		assertThrows(IllegalArgumentException.class, () -> builder.components(""));
	}

	@DisplayName("The number of bytes per component must be a power-of-two")
	@Test
	void bytes() {
		assertThrows(IllegalArgumentException.class, () -> builder.bytes(0));
		assertThrows(IllegalArgumentException.class, () -> builder.bytes(3));
	}

	@Nested
	class NumericFormatTests {
		@DisplayName("The layout component types map to the Vulkan euivalent")
		@Test
		void of() {
			assertEquals(NumericFormat.INT, NumericFormat.of(Layout.Type.INTEGER));
			assertEquals(NumericFormat.FLOAT, NumericFormat.of(Layout.Type.FLOAT));
			assertEquals(NumericFormat.NORM, NumericFormat.of(Layout.Type.NORMALIZED));
		}
	}

	@DisplayName("The builder can construct a format for a given vertex layout")
	@Test
	void layout() {
		final var layout = new Layout(3, Layout.Type.FLOAT, true, 2);
		assertEquals(VkFormat.R16G16B16_SFLOAT, builder.init(layout).build());
	}

	@DisplayName("The builder can determine the format for an image using the hint")
	@Test
	void hint() {
		final ImageData image = new ImageData(new Dimensions(1, 1), "BGR", Layout.floats(3), new byte[1]) {
			@Override
			public int format() {
				return VkFormat.R32G32B32_UINT.value();
			}
		};
		assertEquals(VkFormat.R32G32B32_UINT, FormatBuilder.format(image));
	}

	@DisplayName("The builder can determine the format for an image where the hint is not provided")
	@Test
	void image() {
		final var image = new ImageData(new Dimensions(1, 1), "BGR", Layout.floats(3), new byte[1]);
		assertEquals(VkFormat.R32G32B32_SFLOAT, FormatBuilder.format(image));
	}
}
