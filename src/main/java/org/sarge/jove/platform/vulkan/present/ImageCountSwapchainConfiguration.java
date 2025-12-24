package org.sarge.jove.platform.vulkan.present;

import static java.util.Objects.requireNonNull;

import java.util.function.ToIntFunction;

import org.sarge.jove.platform.vulkan.VkSurfaceCapabilitiesKHR;
import org.sarge.jove.platform.vulkan.present.Swapchain.Builder;
import org.sarge.jove.platform.vulkan.present.SwapchainManager.SwapchainConfiguration;
import org.sarge.jove.platform.vulkan.present.VulkanSurface.Properties;

/**
 * The <i>image count</i> swapchain configuration selects the number of swapchain attachments.
 * @author Sarge
 */
public record ImageCountSwapchainConfiguration(ToIntFunction<VkSurfaceCapabilitiesKHR> policy) implements SwapchainConfiguration {
	/**
	 * Convenience built-in policies.
	 */
	public enum Policy implements ToIntFunction<VkSurfaceCapabilitiesKHR> {
		MIN,
		MIN_PLUS_ONE,
		MAX;

		@Override
		public int applyAsInt(VkSurfaceCapabilitiesKHR capabilities) {
			final int min = capabilities.minImageCount;
			final int max = capabilities.maxImageCount;
			return switch(this) {
				case MIN -> min;
				case MAX -> max;
				case MIN_PLUS_ONE -> {
					if(min == max) {
						throw new RuntimeException();
					}
					yield min + 1;
				}
			};
		}
	}

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
