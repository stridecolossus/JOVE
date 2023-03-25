package org.sarge.jove.platform.vulkan.image;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;

public class MockImage implements Image {
	public final Descriptor.Builder descriptor = new Descriptor.Builder()
			.aspect(VkImageAspect.COLOR)
			.format(VkFormat.R32G32B32A32_SFLOAT)
			.extents(new Dimensions(2, 3));

	@Override
	public Handle handle() {
		return new Handle(1);
	}

	@Override
	public Descriptor descriptor() {
		return descriptor.build();
	}
}
