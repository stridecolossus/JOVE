package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

public class VersionTest {
	private Version ver;

	@BeforeEach
	void before() {
		ver = new Version(1, 2, 3);
	}

	@Test
	void constructor() {
		assertEquals(1, ver.major());
		assertEquals(2, ver.minor());
		assertEquals(3, ver.patch());
	}

	@Test
	void def() {
		assertEquals(new Version(1, 0, 0), Version.DEFAULT);
	}

	@Test
	void toInteger() {
		assertEquals((1 << 22) | (2 << 12) | 3, ver.toInteger());
	}

	@Test
	void equals() {
		assertEquals(ver, ver);
		assertEquals(ver, new Version(1, 2, 3));
		assertNotEquals(ver, null);
		assertNotEquals(ver, new Version(4, 5,6));
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
		assertEquals(-4096, ver.compareTo(new Version(1, 3, 3)));
		assertEquals(+4096, ver.compareTo(new Version(1, 1, 3)));
	}
}
