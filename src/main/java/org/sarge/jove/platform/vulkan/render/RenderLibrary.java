package org.sarge.jove.platform.vulkan.render;

/**
 * Aggregated interface for the rendering package.
 * @author Sarge
 */
public interface RenderLibrary extends Surface.Library, Swapchain.Library, RenderPass.Library, FrameBuffer.Library, DescriptorSet.Library, DrawCommand.Library {
	// Aggregate interface
}
