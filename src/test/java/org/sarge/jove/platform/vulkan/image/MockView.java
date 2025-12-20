package org.sarge.jove.platform.vulkan.image;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.util.Mockery;

public class MockView extends View {
	public MockView() {
		final var library = new Mockery(View.Library.class).proxy();
		super(new Handle(1), new MockLogicalDevice(library), new MockImage(), false);
	}
}
