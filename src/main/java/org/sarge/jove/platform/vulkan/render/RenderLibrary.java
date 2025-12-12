package org.sarge.jove.platform.vulkan.render;

import org.sarge.jove.platform.vulkan.present.Swapchain;

/**
 * Aggregated interface for the rendering package.
 * @author Sarge
 */
public interface RenderLibrary extends Swapchain.Library, RenderPass.Library, Framebuffer.Library, DescriptorSet.Library, DrawCommand.Library {
	// Aggregate interface
}
