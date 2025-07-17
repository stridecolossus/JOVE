package org.sarge.jove.platform.vulkan.core;

import java.util.function.Consumer;

import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.image.ImageLibrary;
import org.sarge.jove.platform.vulkan.memory.MemoryLibrary;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLibrary;
import org.sarge.jove.platform.vulkan.render.RenderLibrary;
import org.sarge.jove.platform.vulkan.util.VulkanException;

/**
 * The <i>Vulkan library</i> is an aggregated interface defining the Vulkan API.
 * @author Sarge
 */
public interface VulkanLibrary extends DeviceLibrary, GeneralLibrary, MemoryLibrary, ImageLibrary, PipelineLibrary, RenderLibrary {
	/**
	 * Vulkan API version.
	 */
	Version VERSION = new Version(1, 1, 0);

	/**
	 * Creates the Vulkan service.
	 * The success code of <b>all</b> native methods is validated by {@link #check(int)}.
	 * @return Vulkan service
	 * @throws RuntimeException if Vulkan cannot be instantiated
	 * TODO - document exception properly
	 */
	static VulkanLibrary create() {
		// Init API factory
		final Registry registry = DefaultRegistry.create();
		final var factory = new NativeLibraryFactory("vulkan-1", registry);

		// Handle success codes
		final Consumer<Object> handler = code -> {
			if((code instanceof VkResult result) && (result != VkResult.SUCCESS)) {
				throw new VulkanException(result);
			}
		};
		factory.setReturnValueHandler(handler);

		// Build API proxy
		return factory.build(VulkanLibrary.class);
	}
	// TODO - registry should be injected, e.g. if want to use other types?

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

interface DeviceLibrary extends Instance.Library, PhysicalDevice.Library, Surface.Library, LogicalDevice.Library {
	// Aggregate API
}

interface GeneralLibrary extends Command.Library, VulkanSemaphore.Library, Fence.Library, VulkanBuffer.Library, Query.Library {
	// Aggregate API
}
