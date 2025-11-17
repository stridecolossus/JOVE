package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.function.ToIntFunction;

import org.sarge.jove.platform.vulkan.VkSurfaceCapabilitiesKHR;
import org.sarge.jove.platform.vulkan.core.VulkanSurface.Properties;
import org.sarge.jove.platform.vulkan.render.Swapchain.Builder;
import org.sarge.jove.platform.vulkan.render.SwapchainFactory.SwapchainConfiguration;

/**
 * The <i>image count swapchain configuration</i> selects the number of swapchain attachments.
 * @author Sarge
 */
public record ImageCountSwapchainConfiguration(ToIntFunction<VkSurfaceCapabilitiesKHR> policy) implements SwapchainConfiguration {
	/**
	 * Selects the minimum image count.
	 */
	public static final ToIntFunction<VkSurfaceCapabilitiesKHR> MIN = capabilities -> capabilities.minImageCount;

	/**
	 * Selects the maximum image count.
	 */
	public static final ToIntFunction<VkSurfaceCapabilitiesKHR> MAX = capabilities -> capabilities.maxImageCount;

	/**
	 * Constructor.
	 * @param policy Image count policy
	 */
	public ImageCountSwapchainConfiguration {
		requireNonNull(policy);
	}

	@Override
	public void configure(Builder builder, Properties properties) {
		final int count = policy.applyAsInt(properties.capabilities());
		builder.count(count);
	}
}
