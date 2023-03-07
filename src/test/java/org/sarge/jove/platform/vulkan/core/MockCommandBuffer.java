package org.sarge.jove.platform.vulkan.core;

import java.util.Set;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.MockDeviceContext;

public class MockCommandBuffer extends Command.Buffer {
	/**
	 * Constructor.
	 */
	public MockCommandBuffer() {
		super(new Handle(1), create(), true);
	}

	private static Command.Pool create() {
		final var family = new WorkQueue.Family(0, 1, Set.of());
		final var queue = new WorkQueue(new Handle(2), family);
		return new Command.Pool(new Handle(3), new MockDeviceContext(), queue);
	}
}
