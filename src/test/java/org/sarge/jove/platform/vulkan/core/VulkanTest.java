package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.util.*;

class VulkanTest {
	private Vulkan vulkan;

	@BeforeEach
	void before() {
		vulkan = new MockVulkan();
	}

	@Test
	void create() {
	}

	@Test
	void check() {
		Vulkan.check(0);
	}

	@Test
	void failed() {
		assertThrows(VulkanException.class, () -> Vulkan.check(-1));
	}

	@Test
	void function() {
		final VulkanFunction<String[]> function = (count, data) -> {
			if(data == null) {
				assertEquals(1, count.value());
			}
			else {
				data[0] = "string";
			}
			return 0;
		};
		assertArrayEquals(new String[]{"string"}, vulkan.invoke(function, String[]::new));
	}

	@Test
	void alignment() {
		Vulkan.checkAlignment(0);
		Vulkan.checkAlignment(4);
		Vulkan.checkAlignment(8);
		assertThrows(IllegalArgumentException.class, () -> Vulkan.checkAlignment(1));
		assertThrows(IllegalArgumentException.class, () -> Vulkan.checkAlignment(2));
		assertThrows(IllegalArgumentException.class, () -> Vulkan.checkAlignment(3));
	}

	@Test
	void layers() {
	}
	// TODO - how
	//when(vulkan.library().vkEnumerateInstanceLayerProperties(null, null))

	@Test
	void extensions() {
	}
}
