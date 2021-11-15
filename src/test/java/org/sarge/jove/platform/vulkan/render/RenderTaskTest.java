package org.sarge.jove.platform.vulkan.render;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFenceCreateInfo;
import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.Command.Pool;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

import com.sun.jna.Pointer;

public class RenderTaskTest extends AbstractVulkanTest {
	private RenderTask loop;
	private Swapchain swapchain;
	private Buffer buffer;
	private Queue queue;

	@BeforeEach
	void before() {
		// Create swapchain
		swapchain = mock(Swapchain.class);
		when(swapchain.device()).thenReturn(dev);
		when(swapchain.count()).thenReturn(2);

		// Create presentation queue
		queue = new Queue(new Handle(1), new Family(2, 3, Set.of()));

		// Create command pool
		final Pool pool = mock(Pool.class);
		when(pool.queue()).thenReturn(queue);
		when(pool.device()).thenReturn(dev);

		// Create render command sequence
		buffer = mock(Buffer.class);
		when(buffer.handle()).thenReturn(new Handle(4));
		when(buffer.pool()).thenReturn(pool);
		when(buffer.isReady()).thenReturn(true);

		// Create render loop
		loop = new RenderTask(swapchain, 2, ignored -> buffer, queue);
	}

	@Test
	void constructor() {
		// TODO
		//final Frame[] frames = loop.frames();
		verify(lib, times(2 * 2)).vkCreateSemaphore(eq(dev), isA(VkSemaphoreCreateInfo.class), isNull(), eq(POINTER));
		verify(lib, times(2)).vkCreateFence(eq(dev), isA(VkFenceCreateInfo.class), isNull(), eq(POINTER));
	}

	@Test
	void execute() {
		loop.execute();
		verify(lib, times(2)).vkWaitForFences(eq(dev), eq(1), isA(Pointer.class), eq(VulkanBoolean.TRUE), eq(Long.MAX_VALUE));
		verify(lib).vkResetFences(eq(dev), eq(1), isA(Pointer.class));

//		final ArgumentCaptor<Semaphore> ready = ArgumentCaptor.forClass(Semaphore.class);
//		verify(swapchain).acquire(ready.capture(), isNull());
//
//		verify(lib).vkQueueSubmit(pool.queue(), array.length, array, fence));
//
//		verify(swapchain).present(queue, 0, Set.of(ready.getValue()));
	}

	@Test
	void close() {
		loop.close();
	}
}
