package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.sarge.jove.platform.vulkan.VkSurfaceFormatKHR;
import org.sarge.jove.platform.vulkan.core.VulkanSurface.Properties;
import org.sarge.jove.platform.vulkan.render.SwapchainFactory.SwapchainConfiguration;

/**
 * The <i>surface format swapchain configuration</i> selects a preferred surface format for the swapchain.
 * @see SurfaceFormatWrapper
 * @author Sarge
 */
public class SurfaceFormatSwapchainConfiguration implements SwapchainConfiguration {
	private final List<Predicate<VkSurfaceFormatKHR>> matchers;

	/**
	 * Constructor.
	 * @param matchers Surface format matchers in order of preference
	 */
	public SurfaceFormatSwapchainConfiguration(List<Predicate<VkSurfaceFormatKHR>> matchers) {
		this.matchers = requireNonNull(matchers);
	}

	/**
	 * Constructor for the common case a single preferred surface format.
	 * @param format Preferred surface format
	 */
	public SurfaceFormatSwapchainConfiguration(VkSurfaceFormatKHR format) {
		final var wrapper = new SurfaceFormatWrapper(format);
		this(List.of(wrapper::equals));
	}

	@Override
	public void configure(Swapchain.Builder builder, Properties properties) {
		// Match available formats
		final var matcher = new Object() {
			private Stream<VkSurfaceFormatKHR> stream(Predicate<VkSurfaceFormatKHR> matcher) {
				return properties
						.formats()
						.stream()
						.filter(matcher);
			}
		};

		// Select matching format
		final VkSurfaceFormatKHR selected = matchers
				.stream()
				.flatMap(matcher::stream)
				.findAny()
				.or(() -> fallback(properties))
				.orElseThrow();

		// Apply
		builder.format(selected);
	}

	/**
	 * Selects the <b>first</b> available format as a fallback.
	 * @param properties Surface properties
	 * @return Fallback format
	 */
	protected static Optional<VkSurfaceFormatKHR> fallback(Properties properties) {
		return properties
				.formats()
				.stream()
				.findAny();
	}
}
