package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Queue;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class WorkTest extends AbstractVulkanTest {
	private Work.Builder builder;
	private Command.Buffer buffer;

	@BeforeEach
	void before() {
		builder = new Work.Builder();
		buffer = create();
	}

	private static Command.Buffer create() {
		final Command.Pool pool = mock(Command.Pool.class);
		final Command.Buffer buffer = mock(Command.Buffer.class);
		final Queue queue = mock(Queue.class);
		when(buffer.pool()).thenReturn(pool);
		when(pool.queue()).thenReturn(queue);
		return buffer;
	}

	@Test
	void build() {
		builder.add(buffer);
		assertNotNull(builder.build());
	}

	@Test
	void buildEmptyBuffers() {
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}

	@Test
	void buildInvalidQueue() {
		builder.add(buffer);
		final Command.Buffer other = create();
		assertThrows(IllegalArgumentException.class, () -> builder.add(other));
	}

	@Test
	void submit() {
		when(buffer.pool().queue().device()).thenReturn(dev);
		final Work work = builder.add(buffer).build();
		work.submit();
	}
}
