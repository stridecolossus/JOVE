package org.sarge.jove.platform.vulkan.image;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;

public class MockImage extends AbstractTransientObject implements Image {
	public final Descriptor.Builder descriptor = new Descriptor.Builder()
			.aspect(VkImageAspectFlags.COLOR)
			.format(VkFormat.B8G8R8A8_UNORM)
			.extents(new Dimensions(640, 480));

	@Override
	public Handle handle() {
		return new Handle(3);
	}

	@Override
	public Descriptor descriptor() {
		return descriptor.build();
	}
}
