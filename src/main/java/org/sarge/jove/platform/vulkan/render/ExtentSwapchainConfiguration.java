package org.sarge.jove.platform.vulkan.render;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.desktop.Window;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.VulkanSurface.Properties;
import org.sarge.jove.platform.vulkan.render.Swapchain.Builder;
import org.sarge.jove.platform.vulkan.render.SwapchainFactory.SwapchainConfiguration;

/**
 * The <i>extent</i> swapchain configuration selects the swapchain extent.
 * <p>
 * The swapchain extents are configured as the {@link VkSurfaceCapabilitiesKHR#currentExtent} if this value is less than {@link Integer#MAX_VALUE}.
 * <p>
 * Otherwise the extents are set to the size of the GLFW <i>framebuffer</i> retrieved via {@link Window#size()}.
 * The window dimensions are clamped to the {@link VkSurfaceCapabilitiesKHR#minImageExtent} and {@link VkSurfaceCapabilitiesKHR#maxImageExtent}.
 * <p>
 * @author Sarge
 */
public class ExtentSwapchainConfiguration implements SwapchainConfiguration {
	@Override
	public void configure(Builder builder, Properties properties) {
		final var capabilities = properties.capabilities();
		final VkExtent2D current = capabilities.currentExtent;
		if(current.width < Integer.MAX_VALUE) {
			builder.extent(current);
		}
		else {
			final Dimensions size = properties.surface().window().size();
			final var min = capabilities.minImageExtent;
			final var max = capabilities.maxImageExtent;
			final int w = Math.clamp(size.width(), min.width, max.width);
			final int h = Math.clamp(size.height(), min.height, max.height);
			builder.extent(new Dimensions(w, h));
		}
	}
}
