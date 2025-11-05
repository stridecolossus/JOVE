package org.sarge.jove.platform.vulkan.core;

import java.util.List;
import java.util.function.Consumer;

import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.platform.vulkan.render.Swapchain;
import org.sarge.jove.platform.vulkan.util.VulkanException;

/**
 * TODO
 * @author Sarge
 */
public interface Vulkan {
	/**
	 * Vulkan API version.
	 */
	Version VERSION = new Version(1, 1, 0);

	/**
	 * Standard validation layer.
	 */
	String STANDARD_VALIDATION = "VK_LAYER_KHRONOS_validation";

	/**
	 * Instantiates the Vulkan native library.
	 * @return Vulkan library
	 */
	static Object create() {
		// Init API factory
		final Registry registry = DefaultRegistry.create();
		final var factory = new NativeLibraryFactory("vulkan-1", registry);

		// Configure success code handler
		final Consumer<Object> handler = code -> {
			if((code instanceof VkResult result) && (result != VkResult.SUCCESS)) {
				throw new VulkanException(result);
			}
		};
		factory.handler(handler);

		// Enumerate API
		final Class<?>[] api = {
				Instance.Library.class,
				PhysicalDevice.Library.class,
				VulkanSurface.Library.class,
				LogicalDevice.Library.class,
				Swapchain.Library.class,
				View.Library.class,
				// TODO...
		};

		// Build native Vulkan API
		return factory.build(List.of(api));
	}

	/**
	 * Checks that the given size is aligned to four bytes.
	 * @param size Buffer offset or size
	 * @throws IllegalArgumentException if the given size is not a multiple of 4 bytes
	 */
	static void checkAlignment(long size) {
		if((size % 4) != 0) {
			throw new IllegalArgumentException("Expected 4-byte alignment");
		}
	}
}
