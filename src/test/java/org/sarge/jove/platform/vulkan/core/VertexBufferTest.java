package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class VertexBufferTest extends AbstractVulkanTest {
	private static final Set<VkBufferUsageFlag> FLAGS = Set.of(VkBufferUsageFlag.VERTEX_BUFFER, VkBufferUsageFlag.UNIFORM_BUFFER);
	private static final long SIZE = 4;

	private VulkanBuffer buffer;
	private DeviceMemory mem;
	private VertexBuffer vbo;

	@BeforeEach
	void before() {
		mem = mock(DeviceMemory.class);
		buffer = new VulkanBuffer(new Handle(1), dev, FLAGS, mem, SIZE);
		vbo = new VertexBuffer(buffer);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(1), vbo.handle());
		assertEquals(dev, vbo.device());
		assertEquals(FLAGS, vbo.usage());
		assertEquals(mem, vbo.memory());
		assertEquals(SIZE, vbo.length());
	}

	@Test
	void bind() {
		final Command.Buffer cmd = mock(Command.Buffer.class);
		final Command bind = vbo.bind(2);
		assertNotNull(bind);
		bind.execute(lib, cmd);
		verify(lib).vkCmdBindVertexBuffers(cmd, 2, 1, NativeObject.array(List.of(buffer)), new long[]{0});
	}
}
