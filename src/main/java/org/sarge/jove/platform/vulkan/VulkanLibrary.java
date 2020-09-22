package org.sarge.jove.platform.vulkan;

import java.util.HashMap;
import java.util.Map;

import org.sarge.jove.platform.IntegerEnumeration;

import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Structure;

/**
 * Vulkan API.
 */
interface VulkanLibrary extends Library, VulkanLibrarySystem, VulkanLibraryGraphics, VulkanLibraryUtility {
	/**
	 * Vulkan API version.
	 */
	Version VERSION = new Version(1, 0, 2);

	/**
	 * Successful result code.
	 */
	int SUCCESS = VkResult.VK_SUCCESS.value();

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
	 * @return Vulkan API
	 */
	public static VulkanLibrary create() {
		final Map<String, Object> options = new HashMap<>();
		options.put(Library.OPTION_TYPE_MAPPER, VulkanStructure.MAPPER);
		return Native.load(library(), VulkanLibrary.class, options);
	}

	/**
	 * @return Vulkan library name
	 */
	private static String library() {
		switch(Platform.getOSType()) {
		case Platform.WINDOWS:		return "vulkan-1";
		case Platform.LINUX:		return "libvulkan";
		default:					throw new UnsupportedOperationException("Unsupported platform: " + Platform.getOSType());
		}
	}

	/**
	 * Base-class Vulkan JNA structure.
	 * Note that this class must be defined as a member of the associated API in order for the type mappers to work correctly.
	 */
	abstract class VulkanStructure extends Structure {
		private static final DefaultTypeMapper MAPPER = new DefaultTypeMapper();

		static {
			MAPPER.addTypeConverter(VulkanBoolean.class, VulkanBoolean.CONVERTER);
			MAPPER.addTypeConverter(IntegerEnumeration.class, IntegerEnumeration.CONVERTER);
		}

		protected VulkanStructure() {
			super(MAPPER);
		}
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
