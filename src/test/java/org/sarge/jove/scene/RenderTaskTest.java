package org.sarge.jove.scene;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.scene.RenderTask.Frame;

public class RenderTaskTest extends AbstractVulkanTest {
	private RenderTask task;
	private Frame frame;

	@BeforeEach
	void before() {
		frame = mock(Frame.class);
		task = new RenderTask(2, () -> frame);
	}

	@Test
	void execute() {
		task.execute();
		task.execute();
		verify(frame, times(2)).render();
	}

	@Test
	void close() {
		task.close();
		verify(frame, times(2)).destroy();
	}
}
