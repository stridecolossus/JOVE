package org.sarge.jove.platform.vulkan.core;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.image.ImageLibrary;
import org.sarge.jove.platform.vulkan.memory.MemoryLibrary;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLibrary;
import org.sarge.jove.platform.vulkan.render.RenderLibrary;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.*;

import com.sun.jna.*;

/**
 * Vulkan API.
 * @author Sarge
 */
public interface VulkanLibrary extends Library, DeviceLibrary, GraphicsLibrary, MemoryLibrary, UtilityLibrary {
	/**
	 * Vulkan API version.
	 */
	Version VERSION = new Version(1, 1, 0);

	/**
	 * Successful result code.
	 */
	int SUCCESS = VkResult.SUCCESS.value();

	/**
	 * Type mapper for custom JOVE types.
	 */
	TypeMapper MAPPER = mapper();

	/**
	 * @return Type mapper for custom JOVE types
	 */
	private static TypeMapper mapper() {
		final var mapper = new DefaultTypeMapper();
		mapper.addTypeConverter(Boolean.class, new NativeBooleanConverter());
		mapper.addTypeConverter(IntEnum.class, IntEnum.CONVERTER);
		mapper.addTypeConverter(BitMask.class, BitMask.CONVERTER);
		mapper.addTypeConverter(Handle.class, Handle.CONVERTER);
		mapper.addTypeConverter(NativeObject.class, NativeObject.CONVERTER);
		return mapper;
	}

	/**
	 * Instantiates the Vulkan API.
	 * @return Vulkan API
	 */
	static VulkanLibrary create() {
		final String name = switch(Platform.getOSType()) {
			case Platform.WINDOWS -> "vulkan-1";
			case Platform.LINUX -> "libvulkan";
			default -> throw new UnsupportedOperationException("Unsupported platform: " + Platform.getOSType());
		};
		return Native.load(name, VulkanLibrary.class, options());
	}

	/**
	 * Helper - Creates a mutable JNA library options map including the Vulkan type {@link #MAPPER}.
	 * @return Library options
	 */
	static Map<String, ?> options() {
		final var options = new HashMap<String, Object>();
		options.put(Library.OPTION_TYPE_MAPPER, MAPPER);
		return options;
	}

	/**
	 * Checks the result of a Vulkan API method.
	 * @param result Result code
	 * @throws VulkanException if the given result is not {@link VkResult#SUCCESS}
	 */
	static void check(int result) throws VulkanException {
		if(result != SUCCESS) {
			throw new VulkanException(result);
		}
	}

	/**
	 * Populates a 2D Vulkan rectangle.
	 * @param rectangle			JOVE Rectangle
	 * @param structure			Vulkan rectangle
	 */
	static void populate(Rectangle rectangle, VkRect2D structure) {
		structure.offset.x = rectangle.x();
		structure.offset.y = rectangle.y();
		structure.extent.width = rectangle.width();
		structure.extent.height = rectangle.height();
	}

	/**
	 * @param size Buffer offset or size
	 * @throws IllegalArgumentException if the give size is not a multiple of 4 bytes
	 */
	static void checkAlignment(long size) {
		if((size % 4) != 0) {
			throw new IllegalArgumentException("Buffer offset/size must be a multiple of four bytes");
		}
	}
}

interface GraphicsLibrary extends PipelineLibrary, ImageLibrary, RenderLibrary {
	// Aggregate library
}

interface DeviceLibrary extends Instance.Library, PhysicalDevice.Library, LogicalDevice.Library {
	// Aggregate library
}

interface UtilityLibrary extends VulkanBuffer.Library, Command.Library, Semaphore.Library, Fence.Library, Query.Library {
	// Aggregate library
}
