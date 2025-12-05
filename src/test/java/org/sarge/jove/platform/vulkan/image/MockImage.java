package org.sarge.jove.platform.vulkan.image;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;

public class MockImage implements Image {
	public final Descriptor.Builder descriptor = new Descriptor.Builder()
			.aspect(VkImageAspectFlags.COLOR)
			.format(VkFormat.R32G32B32A32_SFLOAT)
			.extents(new Dimensions(640, 480));

	public boolean destroyed;

	@Override
	public Handle handle() {
		return new Handle(3);
	}

	@Override
	public Descriptor descriptor() {
		return descriptor.build();
	}

	@Override
	public void destroy() {
		destroyed = true;
	}
}
