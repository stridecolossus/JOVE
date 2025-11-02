package org.sarge.jove.platform.vulkan.core;

import java.util.List;
import java.util.function.Consumer;

import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.common.Version;
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
	 * Instantiates the Vulkan native library.
	 * @return Vulkan library
	 */
	static NativeLibrary create() {
		// Init API factory
		final Registry registry = DefaultRegistry.create();
		final var builder = new NativeLibrary.Builder("vulkan-1", registry);

		// Enumerate API
		final Class<?>[] api = {
				Instance.Library.class,
				PhysicalDevice.Library.class,
				VulkanSurface.Library.class,
				LogicalDevice.Library.class,
				// TODO...
		};

		// Build native Vulkan API
		final NativeLibrary lib = builder.build(List.of(api));

		// Configure success code handler
		final Consumer<Object> handler = code -> {
			if((code instanceof VkResult result) && (result != VkResult.SUCCESS)) {
				throw new VulkanException(result);
			}
		};
		lib.handler(handler);

		return lib;
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
