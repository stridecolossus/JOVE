package org.sarge.jove.platform.vulkan.api;

import java.util.Map;

import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.VkLayerProperties;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;

import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Structure;
import com.sun.jna.TypeMapper;

/**
 * Vulkan API.
 */
public interface VulkanLibrary extends Library, VulkanLibrarySystem, VulkanLibraryGraphics, VulkanLibraryUtility {
	/**
	 * Vulkan API version.
	 */
	Version VERSION = new Version(1, 0, 2);

	/**
	 * Successful result code.
	 */
	int SUCCESS = VkResult.VK_SUCCESS.value();

	/**
	 * Debug utility extension.
	 */
	String EXTENSION_DEBUG_UTILS = "VK_EXT_debug_utils";

	/**
	 * Swap-chain extension.
	 */
	String EXTENSION_SWAP_CHAIN = "VK_KHR_swapchain";

	/**
	 * Identifier for a Vulkan integration test.
	 */
	String INTEGRATION_TEST = "vulkan-integration-test";

//    public static final int
//        VK_MAX_PHYSICAL_DEVICE_NAME_SIZE = 256,
//        VK_UUID_SIZE                     = 16,
//        VK_MAX_EXTENSION_NAME_SIZE       = 256,
//        VK_MAX_DESCRIPTION_SIZE          = 256,
//        VK_MAX_MEMORY_TYPES              = 32,
//        VK_MAX_MEMORY_HEAPS              = 16,
//        VK_REMAINING_MIP_LEVELS          = (~0),
//        VK_REMAINING_ARRAY_LAYERS        = (~0),
//        VK_ATTACHMENT_UNUSED             = (~0),
//        VK_TRUE                          = 1,
//        VK_FALSE                         = 0,
//        VK_QUEUE_FAMILY_IGNORED          = (~0),
//        VK_SUBPASS_EXTERNAL              = (~0);
//    public static final float VK_LOD_CLAMP_NONE = 1000.0f;
//    public static final long VK_WHOLE_SIZE = (~0L);

	/**
	 * Function to retrieve the available extensions for this Vulkan implementation.
	 */
	VulkanFunction<VkExtensionProperties> EXTENSIONS = (api, count, array) -> api.vkEnumerateInstanceExtensionProperties(null, count, array);

	/**
	 * Function to retrieve the available validation layers for this Vulkan implementation.
	 */
	VulkanFunction<VkLayerProperties> LAYERS = (api, count, array) -> api.vkEnumerateInstanceLayerProperties(count, array);

	/**
	 * Type mapper for custom JOVE types.
	 */
	TypeMapper MAPPER = mapper();

	/**
	 * @return Type mapper for custom JOVE types
	 */
	private static TypeMapper mapper() {
		final DefaultTypeMapper mapper = new DefaultTypeMapper();
		mapper.addTypeConverter(VulkanBoolean.class, VulkanBoolean.CONVERTER);
		mapper.addTypeConverter(IntegerEnumeration.class, IntegerEnumeration.CONVERTER);
		mapper.addTypeConverter(Handle.class, Handle.CONVERTER);
		return mapper;
	}

	/**
	 * Instantiates the Vulkan API.
	 * @return Vulkan API
	 */
	static VulkanLibrary create() {
		return Native.load(library(), VulkanLibrary.class, Map.of(Library.OPTION_TYPE_MAPPER, MAPPER));
	}

	/**
	 * @return Vulkan library name
	 */
	private static String library() {
		return switch(Platform.getOSType()) {
		case Platform.WINDOWS -> "vulkan-1";
		case Platform.LINUX -> "libvulkan";
		default -> throw new UnsupportedOperationException("Unsupported platform: " + Platform.getOSType());
		};
	}

	/**
	 * Base-class Vulkan JNA structure.
	 * Note that this class must be defined as a member of the associated API in order for the type mapper to work correctly.
	 */
	abstract class VulkanStructure extends Structure {
		protected VulkanStructure() {
			super(MAPPER);
		}
	}

	/**
	 * @return Factory for pass-by-reference types used by this API
	 * @see ReferenceFactory#DEFAULT
	 */
	default ReferenceFactory factory() {
		return ReferenceFactory.DEFAULT;
	}

	/**
	 * Checks the result of a Vulkan operation.
	 * @param result Result code
	 * @throws VulkanException if the given result is not {@link VkResult#VK_SUCCESS}
	 */
	static void check(int result) {
		if(result != SUCCESS) {
			throw new VulkanException(result);
		}
	}
}
