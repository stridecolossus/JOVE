package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

public class VulkanBooleanTest {
	@Test
	void toBoolean() {
		assertEquals(true, VulkanBoolean.TRUE.toBoolean());
		assertEquals(false, VulkanBoolean.FALSE.toBoolean());
	}

	@Test
	void ofInteger() {
		assertEquals(VulkanBoolean.TRUE, VulkanBoolean.of(1));
		assertEquals(VulkanBoolean.TRUE, VulkanBoolean.of(-1));
		assertEquals(VulkanBoolean.TRUE, VulkanBoolean.of(999));
		assertEquals(VulkanBoolean.FALSE, VulkanBoolean.of(0));
	}

	@Test
	void ofBoolean() {
		assertEquals(VulkanBoolean.TRUE, VulkanBoolean.of(true));
		assertEquals(VulkanBoolean.FALSE, VulkanBoolean.of(false));
	}

	@Test
	void mapper() {
		assertEquals(Integer.class, VulkanBoolean.CONVERTER.nativeType());
	}

	@Test
	void toNative() {
		assertEquals(1, VulkanBoolean.CONVERTER.toNative(VulkanBoolean.TRUE, null));
		assertEquals(0, VulkanBoolean.CONVERTER.toNative(VulkanBoolean.FALSE, null));
		assertEquals(0, VulkanBoolean.CONVERTER.toNative(null, null));
	}

	@Test
	void fromNative() {
		assertEquals(VulkanBoolean.TRUE, VulkanBoolean.CONVERTER.fromNative(1, null));
		assertEquals(VulkanBoolean.FALSE, VulkanBoolean.CONVERTER.fromNative(0, null));
		assertEquals(VulkanBoolean.FALSE, VulkanBoolean.CONVERTER.fromNative(null, null));
	}

	@Test
	void equals() {
		assertEquals(true, VulkanBoolean.TRUE.equals(VulkanBoolean.TRUE));
		assertEquals(true, VulkanBoolean.FALSE.equals(VulkanBoolean.FALSE));
		assertEquals(false, VulkanBoolean.TRUE.equals(VulkanBoolean.FALSE));
		assertEquals(false, VulkanBoolean.FALSE.equals(VulkanBoolean.TRUE));
		assertEquals(false, VulkanBoolean.TRUE.equals(null));
		assertEquals(false, VulkanBoolean.FALSE.equals(null));
	}
}
