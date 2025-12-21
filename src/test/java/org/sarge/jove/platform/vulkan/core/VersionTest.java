package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.Version;

class VersionTest {
	private Version ver;

	@BeforeEach
	void before() {
		ver = new Version(1, 2, 3);
	}

	@Test
	void toInteger() {
		assertEquals(4202499, ver.toInteger());
	}

	@Test
	void of() {
		assertEquals(ver, Version.of(4202499));
	}

	@Test
	void equals() {
		assertEquals(ver, ver);
		assertEquals(ver, new Version(1, 2, 3));
		assertNotEquals(ver, null);
		assertNotEquals(ver, new Version(4, 5, 6));
	}

	@Test
	void string() {
		assertEquals("1.2.3", ver.toString());
	}

	@Test
	void compare() {
		assertEquals(0, ver.compareTo(ver));
		assertEquals(-1, ver.compareTo(new Version(1, 2, 4)));
		assertEquals(+1, ver.compareTo(new Version(1, 2, 2)));
		assertEquals(-1, ver.compareTo(new Version(1, 3, 3)));
		assertEquals(+1, ver.compareTo(new Version(1, 1, 3)));
	}
}
