package org.sarge.jove.platform.vulkan.core;

import java.util.List;
import java.util.function.Consumer;

import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.image.ImageLibrary;
import org.sarge.jove.platform.vulkan.render.Swapchain;

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
	static VulkanCoreLibrary create() {
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
				VulkanCoreLibrary.class,
				Swapchain.Library.class,	// TODO
				ImageLibrary.class,
				// TODO...
		};

		// Build native Vulkan API
		return (VulkanCoreLibrary) factory.build(List.of(api));
	}
}
