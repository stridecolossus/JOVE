package org.sarge.jove.platform.vulkan.present;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.IntFunction;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.VulkanSurface.Properties;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.present.Swapchain.*;

/**
 * The <i>swapchain manager</i> recreates and configures the swapchain on-demand.
 * @see Invalidated
 * @see SwapchainConfiguration
 * @author Sarge
 */
public class SwapchainManager implements TransientObject {
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
	private List<View> views;

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
		build();
		assert swapchain != null;
		assert views.size() == swapchain.attachments().size();
	}

	/**
	 * @return Logical device
	 */
	public LogicalDevice device() {
		return device;
	}

	/**
	 * @return Current swapchain
	 */
	public Swapchain swapchain() {
		return swapchain;
	}

	/**
	 * Helper.
	 * Creates a provider for the swapchain colour attachments.
	 * @return Swapchain attachment provider
	 */
	public IntFunction<View> views() {
		return views::get;
	}

	/**
	 * Recreates the swapchain.
	 * @return New swapchain
	 */
	public Swapchain recreate() {
		destroy();
		build();
		return swapchain;
	}

	/**
	 * Applies the swapchain configuration and creates a new instance.
	 */
	private void build() {
		assert swapchain == null;
		assert views == null;

		// Apply swapchain configuration
		for(var c : configuration) {
			c.configure(builder, properties);
		}

		// Recreate swapchain
		swapchain = builder.build(device, properties);

		// Rebuild colour attachment views
		views = swapchain
				.attachments()
				.stream()
				.map(this::view)
				.toList();
	}

	protected View view(Image image) {
		return View.of(device, image);
	}

	@Override
	public void destroy() {
		for(View view : views) {
			view.destroy();
		}
		views = null;

		swapchain.destroy();
		swapchain = null;
	}
}
