package org.sarge.jove.platform.vulkan.core;

import java.util.List;
import java.util.function.Consumer;

import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.image.ImageLibrary;
import org.sarge.jove.platform.vulkan.memory.MemoryLibrary;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLibrary;
import org.sarge.jove.platform.vulkan.render.RenderLibrary;

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
	 * Instantiates the Vulkan native library.
	 * @return Vulkan library
	 */
	static VulkanCoreLibrary create() {
		// Init API factory
		final Registry registry = DefaultRegistry.create();
		final var factory = new NativeLibraryFactory("vulkan-1", registry);

		// Configure success code handler
		final Consumer<Object> handler = code -> {
			if((code instanceof VkResult result) && (result != VkResult.VK_SUCCESS)) {
				throw new VulkanException(result);
			}
		};
		factory.handler(handler);

		// Enumerate API
		final Class<?>[] api = {
				VulkanCoreLibrary.class,
				MemoryLibrary.class,
				ImageLibrary.class,
				PipelineLibrary.class,
				RenderLibrary.class,
				// TODO...
		};

		// Build native Vulkan API
		return (VulkanCoreLibrary) factory.build(List.of(api));
	}
}
