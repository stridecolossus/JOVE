package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Vertex.Layout;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.util.FormatHelper.Type;

class FormatHelperTest {
	private FormatHelper helper;

	@BeforeEach
	void before() {
		helper = new FormatHelper();
	}

	@Test
	void build() {
		final VkFormat format = helper
			.type(Type.INTEGER)
			.template("RGBA")
			.bytes(2)
			.signed(false)
			.build();
		assertEquals(VkFormat.R16G16B16A16_UINT, format);
	}

	@Test
	void buildDefaults() {
		assertEquals(VkFormat.R32G32B32A32_SFLOAT, helper.build());
	}

	@Test
	void buildComponents() {
		assertEquals(VkFormat.R32G32_SFLOAT, helper.count(2).build());
	}

	@Test
	void invalidComponentString() {
		assertThrows(IllegalArgumentException.class, () -> helper.template(""));
		assertThrows(IllegalArgumentException.class, () -> helper.template("cobblers"));
		assertThrows(IllegalArgumentException.class, () -> helper.template("RGBA?"));
	}

	@Test
	void invalidBytesPerComponent() {
		assertThrows(IllegalArgumentException.class, () -> helper.bytes(0));
		assertThrows(IllegalArgumentException.class, () -> helper.bytes(3));
	}

	@Test
	void invalidComponentNumber() {
		assertThrows(IllegalArgumentException.class, () -> helper.template("RGB").count(4).build());
	}

	@Test
	void layout() {
		assertEquals(VkFormat.R32G32B32_SFLOAT, FormatHelper.format(Layout.of(3, Float.class)));
		assertEquals(VkFormat.R8G8_SRGB, FormatHelper.format(Layout.of(2, Byte.class)));
	}

	@Test
	void types() {
		assertEquals(Type.FLOAT, Type.of(Float.class));
		assertEquals(Type.INTEGER, Type.of(Integer.class));
	}

	@Test
	void invalidComponentTypeMapping() {
		assertThrows(IllegalArgumentException.class, () -> Type.of(String.class));
	}
}
