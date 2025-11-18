package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.VulkanSurface.Properties;
import org.sarge.jove.platform.vulkan.render.Swapchain.*;

/**
 * The <i>swapchain factory</i> recreates and configures the swapchain on-demand.
 * @see Invalidated
 * @see SwapchainConfiguration
 * @author Sarge
 */
public class SwapchainFactory implements TransientObject {
	/**
	 * A <i>swapchain configuration</i> is used to select and configure a property of the swapchain prior to construction.
	 */
	public interface SwapchainConfiguration {
		/**
		 * Configures a swapchain property.
		 * @param builder		Swapchain builder
		 * @param properties	Surface properties
		 */
		void configure(Builder builder, Properties properties);
	}

	private final LogicalDevice device;
	private final Properties properties;
	private final Builder builder;
	private final Collection<SwapchainConfiguration> configuration;
	private Swapchain swapchain;

	/**
	 * Constructor.
	 * @param device			Logical device
	 * @param properties		Surface properties
	 * @param builder			Swapchain builder
	 * @param configuration		Swapchain configuration
	 */
	public SwapchainFactory(LogicalDevice device, Properties properties, Builder builder, List<SwapchainConfiguration> configuration) {
		this.device = requireNonNull(device);
		this.properties = requireNonNull(properties);
		this.builder = requireNonNull(builder);
		this.configuration = List.copyOf(configuration);
		this.swapchain = build();
	}

	/**
	 * @return Current swapchain
	 */
	public Swapchain swapchain() {
		return swapchain;
	}

	/**
	 * Recreates the swapchain.
	 */
	public void recreate() {
		swapchain.destroy();
		swapchain = build();
	}

	/**
	 * Applies the swapchain configuration and creates a new instance.
	 * @return New swapchain
	 */
	private Swapchain build() {
		for(var c : configuration) {
			c.configure(builder, properties);
		}

		return builder.build(device, properties);
	}

	@Override
	public void destroy() {
		swapchain.destroy();
		swapchain = null;
	}
}
