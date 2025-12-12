package org.sarge.jove.platform.vulkan.present;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.VulkanSurface.Properties;
import org.sarge.jove.platform.vulkan.present.Swapchain.Builder;
import org.sarge.jove.platform.vulkan.present.SwapchainManager.SwapchainConfiguration;

/**
 * The <i>surface format</i> swapchain configuration selects a preferred surface format for the swapchain.
 * This implementation falls back to the <b>first</b> surface format supported by the surface.
 * @author Sarge
 */
public class SurfaceFormatSwapchainConfiguration implements SwapchainConfiguration {
	private final SurfaceFormatWrapper format;

	/**
	 * Constructor.
	 * @param format Surface format
	 */
	public SurfaceFormatSwapchainConfiguration(SurfaceFormatWrapper format) {
		this.format = requireNonNull(format);
	}

	/**
	 * Constructor.
	 * @param format		Image format
	 * @param space			Colour space
	 */
	public SurfaceFormatSwapchainConfiguration(VkFormat format, VkColorSpaceKHR space) {
		this(new SurfaceFormatWrapper(format, space));
	}

	@Override
	public void configure(Builder builder, Properties properties) {
		final VkSurfaceFormatKHR selected = properties
				.formats()
				.stream()
				.filter(format::equals)
				.findAny()
				.orElse(properties.formats().getFirst());

		builder.format(selected);
	}
}
