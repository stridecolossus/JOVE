package org.sarge.jove.platform.vulkan.present;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.common.AbstractTransientObject;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.VulkanSurface.Properties;
import org.sarge.jove.platform.vulkan.present.Swapchain.*;

/**
 * The <i>swapchain manager</i> recreates and configures the swapchain on-demand.
 * @see Invalidated
 * @see SwapchainConfiguration
 * @author Sarge
 */
public class SwapchainManager extends AbstractTransientObject {
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
	public SwapchainManager(LogicalDevice device, Properties properties, Builder builder, List<SwapchainConfiguration> configuration) {
		this.device = requireNonNull(device);
		this.properties = requireNonNull(properties);
		this.builder = requireNonNull(builder);
		this.configuration = List.copyOf(configuration);
		init();
	}

	private void init() {
		builder.init(properties.capabilities());
		swapchain = build();
	}

	/**
	 * @return Current swapchain
	 */
	public Swapchain swapchain() {
		return swapchain;
	}

	/**
	 * Recreates the swapchain.
	 * @return New swapchain
	 */
	public Swapchain recreate() {
		release();
		swapchain = build();
		return swapchain;
	}

	/**
	 * Applies the swapchain configuration and creates a new instance.
	 */
	protected Swapchain build() {
		assert swapchain == null;

		// Apply swapchain configuration
		for(var c : configuration) {
			c.configure(builder, properties);
		}

		// Recreate swapchain
		return builder.build(device, properties);
	}

	@Override
	protected void release() {
		swapchain.destroy();
		swapchain = null;
	}
}
