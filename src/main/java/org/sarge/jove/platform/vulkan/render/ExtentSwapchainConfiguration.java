package org.sarge.jove.platform.vulkan.render;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.VkExtent2D;
import org.sarge.jove.platform.vulkan.core.VulkanSurface.Properties;
import org.sarge.jove.platform.vulkan.render.Swapchain.Builder;
import org.sarge.jove.platform.vulkan.render.SwapchainFactory.SwapchainConfiguration;

/**
 * The <i>extent</i> swapchain configuration selects the swapchain extent.
 * TODO - current OR GLFW framebuffer
 * @author Sarge
 */
public record ExtentSwapchainConfiguration() implements SwapchainConfiguration {
	@Override
	public void configure(Builder builder, Properties properties) {
		final var capabilites = properties.capabilities();
		final VkExtent2D current = capabilites.currentExtent;
		if(current.width < Integer.MAX_VALUE) {
			//System.out.println("*********** current="+current.width+" "+current.height);
			builder.extent(current);
		}
		else {
			final Dimensions size = properties.surface().window().size();
			//System.out.println("*********** using framebuffer="+size);
			builder.extent(size);
			// TODO - clamp to min/max
		}
	}
}
// TODO - test!
