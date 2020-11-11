package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkCommandBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkSubmitInfo;
import org.sarge.jove.platform.vulkan.core.Work.ImmediateCommand;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class WorkTest extends AbstractVulkanTest {
	private Work.Builder builder;
	private Command.Pool pool;
	private Command.Buffer buffer;
	private Queue queue;

	@BeforeEach
	void before() {
		// Create queue
		queue = mock(Queue.class);
		when(queue.device()).thenReturn(dev);
		when(queue.handle()).thenReturn(new Handle(new Pointer(1)));
		when(queue.family()).thenReturn(mock(Queue.Family.class));

		// Create pool
		pool = mock(Command.Pool.class);
		when(pool.queue()).thenReturn(queue);

		// Create command buffer
		buffer = mock(Command.Buffer.class);
		when(buffer.pool()).thenReturn(pool);
		when(buffer.handle()).thenReturn(new Handle(new Pointer(2)));
		when(buffer.isReady()).thenReturn(true);

		// Create builder
		builder = new Work.Builder(queue);
	}

	@Test
	void build() {
		assertNotNull(builder.add(buffer).build());
	}

	@Test
	void buildEmptyBuffers() {
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}

	@Test
	void buildBufferNotReady() {
		when(buffer.isReady()).thenReturn(false);
		assertThrows(IllegalStateException.class, () -> builder.add(buffer));
	}

	@Test
	void buildInvalidQueue() {
		final Command.Buffer other = mock(Command.Buffer.class);
		when(other.pool()).thenReturn(mock(Command.Pool.class));
		when(other.pool().queue()).thenReturn(mock(Queue.class));
		when(other.isReady()).thenReturn(true);

		builder.add(buffer);
		assertThrows(IllegalArgumentException.class, () -> builder.add(other));
	}

	@Test
	void submit() {
		// Submit work
		builder.add(buffer).build().submit();

		// Check API
		final ArgumentCaptor<VkSubmitInfo[]> captor = ArgumentCaptor.forClass(VkSubmitInfo[].class);
		verify(lib).vkQueueSubmit(eq(queue.handle()), eq(1), captor.capture(), isNull());

		// Check descriptor array
		final VkSubmitInfo[] array = captor.getValue();
		assertNotNull(array);
		assertEquals(1, array.length);

		// Check submit descriptor
		final VkSubmitInfo info = array[0];
		assertNotNull(info);
		assertEquals(1, info.commandBufferCount);
		assertNotNull(info.pCommandBuffers);

		// TODO - synchronization
	}

	@Test
	void immediate() {
		// Create command
		final ImmediateCommand immediate = ImmediateCommand.of(mock(Command.class));

		// Mock buffer
		when(pool.allocate()).thenReturn(buffer);
		when(buffer.begin(VkCommandBufferUsageFlag.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)).thenReturn(buffer);
		when(buffer.add(immediate)).thenReturn(buffer);
		when(buffer.end()).thenReturn(buffer);

		// Submit command and check is executed and released
		immediate.submit(pool, true);
		verify(queue).waitIdle();
		verify(buffer).free();
	}
}
