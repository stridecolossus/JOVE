package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkStructureType;
import org.sarge.jove.util.MockStructure;

public class VulkanStructureTest {
	private MockStructure struct;

	@BeforeEach
	void before() {
		struct = new MockStructure();
	}

	@Test
	void getFieldOrder() {
		assertEquals(List.of("sType"), struct.getFieldOrder());
	}

	@Test
	void copy() {
		final MockStructure copy = struct.copy();
		assertNotNull(copy);
		assertEquals(VkStructureType.APPLICATION_INFO, copy.sType);
	}
}
