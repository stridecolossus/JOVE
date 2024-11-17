package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.util.VulkanException;

class VulkanTest {
	private Vulkan vulkan;

	@BeforeEach
	void before() {
		vulkan = new MockVulkan();
	}

	@Test
	void library() {
		assertNotNull(vulkan.library());
	}

	@Test
	void registry() {
		assertNotNull(vulkan.registry());
	}

	@Test
	void factory() {
		assertNotNull(vulkan.factory());
	}

	@Test
	void layers() {
	}

	@Test
	void extensions() {
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
	void create() {
	}
}
