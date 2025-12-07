package org.sarge.jove.platform.vulkan.render;

import java.util.List;

import org.sarge.jove.platform.vulkan.VkPresentModeKHR;
import org.sarge.jove.platform.vulkan.core.VulkanSurface.Properties;
import org.sarge.jove.platform.vulkan.render.SwapchainFactory.SwapchainConfiguration;

/**
 * The <i>presentation mode</i> swapchain configuration selects a preferred presentation mode for the swapchain.
 * Falls back to {@link Swapchain#DEFAULT_PRESENTATION_MODE} if none of the preferred mode(s) are supported by the surface.
 * @author Sarge
 */
public class PresentationModeSwapchainConfiguration implements SwapchainConfiguration {
	private final List<VkPresentModeKHR> modes;

	/**
	 * Constructor.
	 * @param modes Presentation modes (most preferred first)
	 */
	public PresentationModeSwapchainConfiguration(List<VkPresentModeKHR> modes) {
		this.modes = List.copyOf(modes);
	}

	@Override
	public void configure(Swapchain.Builder builder, Properties properties) {
		final VkPresentModeKHR selected = properties
				.modes()
				.stream()
				.filter(modes::contains)
				.findAny()
				.orElse(Swapchain.DEFAULT_PRESENTATION_MODE);

		builder.presentation(selected);
	}
}
