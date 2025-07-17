package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class VulkanLibraryTest {
	@Test
	void create() {
	}

	@Test
	void alignment() {
		VulkanLibrary.checkAlignment(0);
		VulkanLibrary.checkAlignment(4);
		VulkanLibrary.checkAlignment(8);
		assertThrows(IllegalArgumentException.class, () -> VulkanLibrary.checkAlignment(1));
		assertThrows(IllegalArgumentException.class, () -> VulkanLibrary.checkAlignment(2));
		assertThrows(IllegalArgumentException.class, () -> VulkanLibrary.checkAlignment(3));
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
