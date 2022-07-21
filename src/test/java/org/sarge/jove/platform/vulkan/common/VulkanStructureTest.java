package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.*;
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
}
