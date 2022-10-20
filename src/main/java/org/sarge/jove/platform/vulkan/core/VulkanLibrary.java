package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toSet;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.image.ImageLibrary;
import org.sarge.jove.platform.vulkan.memory.MemoryLibrary;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLibrary;
import org.sarge.jove.platform.vulkan.render.*;
import org.sarge.jove.platform.vulkan.util.*;
import org.sarge.jove.platform.vulkan.util.VulkanFunction.StructureVulkanFunction;
import org.sarge.jove.util.IntegerEnumeration;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;

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
	 * Debug utility extension.
	 */
	String EXTENSION_DEBUG_UTILS = "VK_EXT_debug_utils";

	/**
	 * Swap-chain extension.
	 */
	String EXTENSION_SWAP_CHAIN = "VK_KHR_swapchain";

	/**
	 * Type mapper for custom JOVE types.
	 */
	TypeMapper MAPPER = mapper();

	/**
	 * @return Type mapper for custom JOVE types
	 */
	private static TypeMapper mapper() {
		final var mapper = new DefaultTypeMapper();
		mapper.addTypeConverter(Boolean.class, new VulkanBooleanConverter());
		mapper.addTypeConverter(IntegerEnumeration.class, IntegerEnumeration.CONVERTER);
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

		return Native.load(name, VulkanLibrary.class, Map.of(Library.OPTION_TYPE_MAPPER, MAPPER));
	}

	/**
	 * Checks the result of a Vulkan API method.
	 * @param result Result code
	 * @throws VulkanException if the given result is not {@link VkResult#SUCCESS}
	 */
	static void check(int result) {
		if(result != SUCCESS) {
			throw new VulkanException(result);
		}
	}

	/**
	 * Enumerates supported extensions for the Vulkan platform or a physical device.
	 * @param count		Count
	 * @param func 		Extensions function
	 * @return Supported extensions
	 */
	static Set<String> extensions(IntByReference count, StructureVulkanFunction<VkExtensionProperties> func) {
		return Arrays
				.stream(func.invoke(count, VkExtensionProperties::new))
				.map(e -> e.extensionName)
				.map(String::new)
				.collect(toSet());
	}

	/**
	 * Enumerates extensions supported by this platform.
	 * @param lib		Vulkan
	 * @param count		Count
	 * @return Extensions supported by this platform
	 * @see #extensions(IntByReference, VulkanFunction)
	 */
	static Set<String> extensions(VulkanLibrary lib, IntByReference count) {
		final StructureVulkanFunction<VkExtensionProperties> func = (ref, array) -> lib.vkEnumerateInstanceExtensionProperties(null, ref, array);
		return extensions(count, func);
	}
}

interface GraphicsLibrary extends PipelineLibrary, ImageLibrary, RenderLibrary, Surface.Library {
	// Aggregate library
}

interface DeviceLibrary extends Instance.Library, PhysicalDevice.Library, LogicalDevice.Library {
	// Aggregate library
}

interface UtilityLibrary extends VulkanBuffer.Library, Command.Library, Semaphore.Library, Fence.Library, Query.Library {
	// Aggregate library
}
