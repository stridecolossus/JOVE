package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

public class VulkanBooleanTest {
	@Test
	public void isTrue() {
		assertEquals(true, VulkanBoolean.TRUE.isTrue());
		assertEquals(false, VulkanBoolean.FALSE.isTrue());
	}

	@Test
	public void toInteger() {
		assertEquals(1, VulkanBoolean.TRUE.toInteger());
		assertEquals(0, VulkanBoolean.FALSE.toInteger());
	}

	@Test
	public void ofInteger() {
		assertEquals(VulkanBoolean.TRUE, VulkanBoolean.of(1));
		assertEquals(VulkanBoolean.TRUE, VulkanBoolean.of(-1));
		assertEquals(VulkanBoolean.TRUE, VulkanBoolean.of(999));
		assertEquals(VulkanBoolean.FALSE, VulkanBoolean.of(0));
	}

	@Test
	public void ofBoolean() {
		assertEquals(VulkanBoolean.TRUE, VulkanBoolean.of(true));
		assertEquals(VulkanBoolean.FALSE, VulkanBoolean.of(false));
	}

	@Test
	public void mapperNativeType() {
		assertEquals(Integer.class, VulkanBoolean.CONVERTER.nativeType());
	}

	@Test
	public void mapperToNative() {
		assertEquals(1, VulkanBoolean.CONVERTER.toNative(VulkanBoolean.TRUE, null));
		assertEquals(0, VulkanBoolean.CONVERTER.toNative(VulkanBoolean.FALSE, null));
		assertEquals(0, VulkanBoolean.CONVERTER.toNative(null, null));
	}

	@Test
	public void mapperFromNative() {
		assertEquals(VulkanBoolean.TRUE, VulkanBoolean.CONVERTER.fromNative(1, null));
		assertEquals(VulkanBoolean.FALSE, VulkanBoolean.CONVERTER.fromNative(0, null));
		assertEquals(VulkanBoolean.FALSE, VulkanBoolean.CONVERTER.fromNative(null, null));
	}

	@Test
	public void equals() {
		assertEquals(true, VulkanBoolean.TRUE.equals(VulkanBoolean.TRUE));
		assertEquals(true, VulkanBoolean.FALSE.equals(VulkanBoolean.FALSE));
		assertEquals(false, VulkanBoolean.TRUE.equals(VulkanBoolean.FALSE));
		assertEquals(false, VulkanBoolean.FALSE.equals(VulkanBoolean.TRUE));
		assertEquals(false, VulkanBoolean.TRUE.equals(null));
		assertEquals(false, VulkanBoolean.FALSE.equals(null));
	}
}
