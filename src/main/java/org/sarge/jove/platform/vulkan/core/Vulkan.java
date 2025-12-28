package org.sarge.jove.platform.vulkan.core;

import java.util.List;
import java.util.function.Consumer;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.image.ImageLibrary;
import org.sarge.jove.platform.vulkan.memory.MemoryLibrary;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLibrary;
import org.sarge.jove.platform.vulkan.render.RenderLibrary;

/**
 * The <i>Vulkan</i> class is used to create the native library and also provides some common utility helpers.
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

	/**
	 * Builds the view transformation matrix for Vulkan.
	 * <p>
	 * To maintain the right-handed coordinate system the view axis are flipped 180 degrees about the horizontal axis.
	 * This transforms view space to the Vulkan clip space coordinate system where Y is down and the view direction is along the Z axis.
	 * <p>
	 * Generally this is combined with the projection matrix to transform view space after the camera transformation.
	 * <p>
	 * @return Vulkan transformation matrix
	 */
	static Matrix matrix() {
		return new Matrix.Builder()
				.identity()
				.set(1, 1, -1)
				.set(2, 2, -1)
				.build();
	}

	/**
	 * @param size Buffer offset or size
	 * @throws IllegalArgumentException if the given size is not a multiple of 4 bytes
	 */
	static void checkAlignment(long size) {
		if((size % 4) != 0) {
			throw new IllegalArgumentException("Expected 4-byte alignment");
		}
	}

	/**
	 * Helper.
	 * Converts the given dimensions to Vulkan extents.
	 * @param dimensions Dimensions
	 * @return Extents
	 */
	static VkExtent2D extents(Dimensions dimensions) {
		final var extent = new VkExtent2D();
		extent.width = dimensions.width();
		extent.height = dimensions.height();
		return extent;
	}

	/**
	 * Helper.
	 * Converts a rectangle to the Vulkan equivalent.
	 * @param rectangle Rectangle
	 * @return Vulkan rectangle
	 */
	static VkRect2D rectangle(Rectangle rectangle) {
		final VkRect2D result = new VkRect2D();
		result.offset = new VkOffset2D();
		result.offset.x = rectangle.x();
		result.offset.y = rectangle.y();
		result.extent = extents(rectangle.dimensions());
		return result;
	}
}
