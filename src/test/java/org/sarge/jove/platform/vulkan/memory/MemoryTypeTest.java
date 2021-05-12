package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;

public class MemoryTypeTest {
	private MemoryType type;

	@BeforeEach
	void before() {
		type = new MemoryType(1, Set.of(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));
	}

	@Test
	void constructor() {
		assertEquals(1, type.index());
		assertEquals(Set.of(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT), type.properties());
	}

	@Test
	void equals() {
		assertEquals(true, type.equals(type));
		assertEquals(true, type.equals(new MemoryType(1, Set.of(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT))));
		assertEquals(false, type.equals(null));
		assertEquals(false, type.equals(new MemoryType(1, Set.of())));
	}
}
