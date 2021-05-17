package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkSubmitInfo;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Semaphore;
import org.sarge.jove.platform.vulkan.core.View;
import org.sarge.jove.platform.vulkan.render.Runner.Frame;
import org.sarge.jove.platform.vulkan.render.Runner.FrameState;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

@Timeout(value=1, unit=TimeUnit.SECONDS)
public class RunnerTest extends AbstractVulkanTest {
	// Dependencies
	private Swapchain swapchain;
	private View view;
	private Queue queue;

	// Runner
	private Runner runner;
	private Frame frame;
	private Semaphore semaphore;

	// Thread
	private Thread thread;
	private CountDownLatch start, end;

	@BeforeEach
	void before() {
		// Create swapchain
		swapchain = mock(Swapchain.class);
		when(swapchain.device()).thenReturn(dev);

		// Init swapchain views
		view = mock(View.class);
		when(swapchain.views()).thenReturn(List.of(view));

		// Create presentation queue
		queue = new Queue(new Handle(new Pointer(1)), dev, new Queue.Family(0, 1, Set.of()));

		// Create frame
		frame = mock(Frame.class);
		when(frame.update()).thenReturn(true);

		// Mock synchronisation
		semaphore = mock(Semaphore.class);
		when(semaphore.handle()).thenReturn(new Handle(new Pointer(2)));
		when(dev.semaphore()).thenReturn(semaphore);

		// Create runner (note two frames but only one swapchain image)
		runner = new Runner(swapchain, 2, ignored -> frame, queue);

		// Init runner thread
		thread = null;
		start = new CountDownLatch(1);
		end = new CountDownLatch(1);
	}

	@AfterEach
	void after() {
		if((thread != null) && thread.isAlive()) {
			thread.interrupt();
		}
	}

	private void start() throws InterruptedException {
		// Start runner in separate thread
		final Runnable wrapper = () -> {
			runner.start();
			end.countDown();
		};
		thread = new Thread(wrapper);
		thread.start();

		// Wait until running
		// TODO - nasty/flakey
		while(!runner.isRunning()) {
			// Repeat
		}
		start.countDown();
	}

	@Test
	void constructor() {
		assertEquals(false, runner.isRunning());
	}

	@Test
	void frame() {
		// Render a frame
		runner.frame();

		// Check frame is rendered
		final ArgumentCaptor<FrameState> captor = ArgumentCaptor.forClass(FrameState.class);
		verify(frame).render(captor.capture(), eq(view));
		verify(frame).update();

		// Check frame state
		final FrameState state = captor.getValue();
		assertNotNull(state);
		assertNotNull(state.ready());
		assertNotNull(state.finished());
		assertNotNull(state.fence());

		// Check render loop
		verify(swapchain).acquire(state.ready(), state.fence());
		verify(swapchain).present(queue, Set.of(state.finished()));

		// Render to frame
		final Command.Buffer buffer = mock(Command.Buffer.class);
		when(buffer.handle()).thenReturn(new Handle(new Pointer(2)));
		when(buffer.isReady()).thenReturn(true);
		when(buffer.pool()).thenReturn(mock(Command.Pool.class));
		when(buffer.pool().queue()).thenReturn(queue);
		state.render(buffer);
		verify(lib).vkQueueSubmit(eq(queue.handle()), eq(1), isA(VkSubmitInfo[].class), eq(state.fence().handle()));
	}

	@Test
	void run() throws InterruptedException {
		// Stop runner after a single frame
		final Answer<Boolean> answer = inv -> {
			assertEquals(true, runner.isRunning());
			return false;
		};
		doAnswer(answer).when(frame).update();

		// Wait for runner to exit
		start();
		end.await();
		assertEquals(false, runner.isRunning());
	}

	@Test
	void stop() throws InterruptedException {
		when(frame.update()).thenReturn(true);
		start();
		start.await();
		runner.stop();
		end.await();
		assertEquals(false, runner.isRunning());
	}

	@Test
	void destroy() {
		runner.destroy();
		verify(semaphore, times(2 * 2)).destroy();
		// TODO - fence
	}

	@Test
	void destroyRunning() throws InterruptedException {
		start();
		start.await();
		assertThrows(IllegalStateException.class, () -> runner.destroy());
	}
}
