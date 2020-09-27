package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class ReferenceFactoryTest {
	private ReferenceFactory factory;

	@BeforeEach
	void before() {
		factory = ReferenceFactory.DEFAULT;
	}

	@Test
	void integer() {
		final IntByReference integer = factory.integer();
		assertNotNull(integer);
		assertEquals(0, integer.getValue());
	}

	@Test
	void pointer() {
		final PointerByReference ptr = factory.pointer();
		assertNotNull(ptr);
		assertEquals(null, ptr.getValue());
	}

	@Test
	void pointers() {
		final Pointer[] array = factory.pointers(2);
		assertNotNull(array);
		assertEquals(2, array.length);
	}
}
