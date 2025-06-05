package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.memory.*;

public class VertexBufferTest {
	private DeviceContext dev;
	private DeviceMemory mem;
	private VertexBuffer vbo;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		mem = new MockDeviceMemory();
		vbo = new VertexBuffer(new VulkanBuffer(new Handle(1), dev, Set.of(VkBufferUsageFlag.VERTEX_BUFFER, VkBufferUsageFlag.UNIFORM_BUFFER), mem, 2));
	}

	@Test
	void constructor() {
		assertEquals(new Handle(1), vbo.handle());
		assertEquals(dev, vbo.device());
		assertEquals(Set.of(VkBufferUsageFlag.VERTEX_BUFFER, VkBufferUsageFlag.UNIFORM_BUFFER), vbo.usage());
		assertEquals(mem, vbo.memory());
		assertEquals(2, vbo.length());
	}

	@Test
	void bind() {
		final VulkanLibrary lib = dev.library();
		final var cmd = new MockCommandBuffer();
		final Command bind = vbo.bind(2);
		bind.execute(lib, cmd);
		verify(lib).vkCmdBindVertexBuffers(cmd, 2, 1, NativeObject.array(List.of(vbo)), new long[]{0});
	}
}
