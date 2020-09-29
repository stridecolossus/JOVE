package org.sarge.jove.platform.vulkan.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.api.Version;

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
	void toInteger() {
		assertEquals(4202499, ver.toInteger());
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
	}
}
