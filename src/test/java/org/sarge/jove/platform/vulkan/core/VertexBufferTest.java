package org.sarge.jove.platform.vulkan.core;

import org.junit.jupiter.api.*;

class VertexBufferTest {
	private VertexBuffer vertex;

	@BeforeEach
	void before() {
		//final var buffer = VulkanBuffer.create(null, null, 0, null);
		final VulkanBuffer buffer = null; // TODO - mock?
		vertex = new VertexBuffer(buffer);
	}

	@Test
	void bind() {
//		final Command bind = buffer.bind(1);
//		bind.execute(null);
//		assertEquals(true, library.bind);
	}
}
