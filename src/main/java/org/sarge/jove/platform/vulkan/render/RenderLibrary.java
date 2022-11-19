package org.sarge.jove.platform.vulkan.render;

public interface RenderLibrary extends Surface.Library, Swapchain.Library, RenderPass.Library, FrameBuffer.Library, DescriptorLibrary, DrawCommand.Library {
	// Aggregate interface
}

interface DescriptorLibrary extends DescriptorLayout.Library, DescriptorPool.Library, DescriptorSet.Library {
	// Aggregate interface
}
