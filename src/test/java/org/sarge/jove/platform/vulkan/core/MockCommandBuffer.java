package org.sarge.jove.platform.vulkan.core;

import java.util.Set;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.MockDeviceContext;
import org.sarge.jove.platform.vulkan.core.Command.PrimaryBuffer;

public class MockCommandBuffer extends PrimaryBuffer {
	/**
	 * Constructor.
	 */
	public MockCommandBuffer() {
		super(new Handle(1), create());
	}

	private static Command.Pool create() {
		final var family = new WorkQueue.Family(0, 1, Set.of());
		final var queue = new WorkQueue(new Handle(2), family);
		return new Command.Pool(new Handle(3), new MockDeviceContext(), queue);
	}
}
