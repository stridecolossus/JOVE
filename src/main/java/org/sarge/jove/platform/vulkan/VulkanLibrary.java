package org.sarge.jove.platform.vulkan;

import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.HashMap;
import java.util.Map;

import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.lib.util.AbstractEqualsObject;

import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
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
	static VulkanLibrary create() {
		final Map<String, Object> options = new HashMap<>();
		options.put(Library.OPTION_TYPE_MAPPER, VulkanStructure.MAPPER);
		return Native.load("vulkan-1", VulkanLibrary.class, options);
	}

	/**
	 * Base-class Vulkan JNA structure.
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
	 * @throws ServiceException if the given result is not {@link VkResult#VK_SUCCESS}
	 */
	static void check(int result) {
		if(result != SUCCESS) {
			try {
				final VkResult value = IntegerEnumeration.map(VkResult.class, result);
				throw new ServiceException(String.format("Vulkan error: result=%d [%s]", result, value.name()));
			}
			catch(IllegalArgumentException e) {
				throw new ServiceException("Unknown Vulkan error: result=" + result);
			}
		}
	}

	/**
	 * Vulkan version.
	 */
	final class Version extends AbstractEqualsObject implements Comparable<Version> {
		private static final char DELIMITER = '.';

		private final int major, minor, patch;

		/**
		 * Constructor.
		 * @param major
		 * @param minor
		 * @param patch
		 */
		public Version(int major, int minor, int patch) {
			// TODO - shifting => need maximums?
			this.major = zeroOrMore(major);
			this.minor = zeroOrMore(minor);
			this.patch = zeroOrMore(patch);
		}

		/**
		 * @return Packed version integer
		 */
		public int toInteger() {
	        return (major << 22) | (minor << 12) | patch;
		}

		@Override
		public int compareTo(Version that) {
			return this.toInteger() - that.toInteger();
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(major);
			sb.append(DELIMITER);
			sb.append(minor);
			sb.append(DELIMITER);
			sb.append(patch);
			return sb.toString();
		}
	}

	/**
	 * Looks up a function.
	 * @param instance		Vulkan instance
	 * @param name			Function name
	 * @return Function pointer
	 */
	Pointer vkGetInstanceProcAddr(Pointer instance, String name);
}
