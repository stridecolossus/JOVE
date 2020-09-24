package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VulkanTest {
	private Vulkan vulkan;
	private VulkanLibrary api;

	@BeforeEach
	void before() {
		api = mock(VulkanLibrary.class);
		vulkan = new Vulkan(api);
	}

	@Test
	void constructor() {
		assertEquals(api, vulkan.api());
	}

	@Test
	void references() {
		assertNotNull(vulkan.pointer());
		assertNotNull(vulkan.integer());
	}
}
