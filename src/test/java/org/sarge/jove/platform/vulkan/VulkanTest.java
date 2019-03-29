package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.Vulkan.ReferenceFactory;

public class VulkanTest {
	@Tag("Vulkan")
	@Test
	public void init() {
		final Vulkan vulkan = Vulkan.create();
		assertNotNull(vulkan);
		assertNotNull(vulkan.library());
		assertEquals(ReferenceFactory.DEFAULT, vulkan.factory());
		assertNotNull(vulkan.supported());
		assertThrows(IllegalStateException.class, () -> Vulkan.create());
	}
}
