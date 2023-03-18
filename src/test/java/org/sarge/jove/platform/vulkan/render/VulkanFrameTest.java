package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;

class VulkanFrameTest {
	private DeviceContext dev;
	private VulkanFrame frame;
	private Swapchain swapchain;
	private Semaphore available, ready;
	private Fence fence;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		swapchain = mock(Swapchain.class);
		available = Semaphore.create(dev);
		ready = Semaphore.create(dev);
		fence = spy(MockFence.class);
		frame = new VulkanFrame(available, ready, fence);
		when(swapchain.acquire(available, null)).thenReturn(1);
	}

	@DisplayName("A frame can be created for the logical device")
	@Test
	void create() {
		VulkanFrame.create(dev);
	}

	@DisplayName("The ready and available semaphores cannot be the same instance")
	@Test
	void semaphores() {
		assertThrows(IllegalArgumentException.class, () -> new VulkanFrame(ready, ready, fence));
	}

	@DisplayName("A new frame...")
	@Nested
	class Ready {
		@DisplayName("is ready to be acquired")
    	@Test
    	void ready() {
    		// TODO - check fence = signalled
    	}

		@DisplayName("can be acquired")
    	@Test
    	void acquire() {
    		assertEquals(1, frame.acquire(swapchain));
    	}

		@DisplayName("cannot be presented")
		@Test
		void present() {
			assertThrows(IllegalStateException.class, () -> frame.present(mock(Command.Buffer.class)));
		}
	}

	@DisplayName("An acquired frame...")
	@Nested
	class Acquired {
		@BeforeEach
		void before() {
			frame.acquire(swapchain);
		}

		@DisplayName("waits for the previous frame to be completed")
    	@Test
    	void previous() {
			verify(fence).waitReady();
			verify(fence).reset();
    	}

		@DisplayName("cannot be acquired again")
    	@Test
    	void acquire() {
    		assertThrows(IllegalStateException.class, () -> frame.acquire(swapchain));
    	}

		@DisplayName("can be presented on completion of the rendering work")
    	@Test
    	void present() {
    		final WorkQueue queue = new WorkQueue(new Handle(1), new WorkQueue.Family(1, 2, Set.of()));
    		final var pool = Command.Pool.create(dev, queue);
    		final var render = pool.allocate(true).begin().end();
    		frame.present(render);
    		verify(fence, times(2)).waitReady();
    		verify(swapchain).present(queue, 1, ready);
    	}
	}

	@DisplayName("A frame can be destroyed")
	@Test
	void destroy() {
		frame.destroy();
		assertEquals(true, available.isDestroyed());
		assertEquals(true, ready.isDestroyed());
		verify(fence).destroy();
	}
}
