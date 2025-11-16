package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.VkPresentModeKHR;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.render.Swapchain.Invalidated;

/**
 * The <i>swapchain factory</i> recreates the swapchain on-demand when it becomes invalid.
 * @see Invalidated
 * @author Sarge
 */
public class SwapchainFactory implements TransientObject {
	private final LogicalDevice device;
	private final VulkanSurface.Properties properties;

	private Swapchain swapchain;

	/**
	 * Constructor.
	 * @param device			Logical device
	 * @param properties		Surface properties
	 */
	public SwapchainFactory(LogicalDevice device, VulkanSurface.Properties properties) {
		this.device = requireNonNull(device);
		this.properties = requireNonNull(properties);
		init();
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
		init();
	}

	private void init() {
		swapchain = build();
	}

	/**
	 * @return TODO
	 */
	private Swapchain build() {

		// TODO - move all swapchain configuration & logic here -> builder -> new swapchain

		return new Swapchain.Builder(properties)
				.clipped(true)
				.presentation(VkPresentModeKHR.MAILBOX_KHR)
				.clear(new Colour(0.6f, 0.6f, 0.6f, 1))
				.build(device);
	}

	@Override
	public void destroy() {
		swapchain.destroy();
		swapchain = null;
	}

	/**
	 * configurable properties:
	 * - clipped [custom?] or window/surface is full?
	 * - presentation mode ~ surface [selected]
	 * - sharing mode ~ queue families
	 * - extents ~ surface & window
	 * - image count ~ surface [or custom?]
	 * - image format ~ surface [selected]
	 * - clear colour -> views [custom]
	 *
	 * unsure:
	 * - flags?
	 * - usage?
	 * - array layers?
	 * - alpha?
	 *
	 */
}
