package org.sarge.jove.platform.vulkan.image;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

public class MockView extends View {
	public MockView(LogicalDevice device) {
		super(new Handle(1), device, new MockImage());
	}
}
