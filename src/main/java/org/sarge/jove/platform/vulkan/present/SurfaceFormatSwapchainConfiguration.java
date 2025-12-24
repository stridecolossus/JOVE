package org.sarge.jove.platform.vulkan.present;

import static java.util.Objects.requireNonNull;

import java.util.List;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.present.Swapchain.Builder;
import org.sarge.jove.platform.vulkan.present.SwapchainManager.SwapchainConfiguration;
import org.sarge.jove.platform.vulkan.present.VulkanSurface.Properties;
import org.sarge.jove.util.PrioritySelector;

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
		final List<VkSurfaceFormatKHR> formats = properties.formats();
		final var selector = new PrioritySelector<VkSurfaceFormatKHR>(format::equals);		// TODO - configurable/protected
		final VkSurfaceFormatKHR selected = selector.select(formats);
		builder.format(selected);
	}
}
