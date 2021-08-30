package org.sarge.jove.platform.vulkan.api;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.VkLayerProperties;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;
import org.sarge.jove.platform.vulkan.util.VulkanHelper;

import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Structure;
import com.sun.jna.TypeMapper;

/**
 * Vulkan API.
 * @author Sarge
 */
public interface VulkanLibrary extends Library, VulkanLibrarySystem, VulkanLibraryGraphics, VulkanLibraryUtility {
	/**
	 * Vulkan API version.
	 */
	Version VERSION = new Version(1, 1, 0);

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
	// TODO - or expose and allow public mutable? i.e. less inter-dependencies?

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
	 * @param lib Vulkan
	 * @return Extensions supported by this platform
	 */
	static Set<String> extensions(VulkanLibrary lib) {
		final VulkanFunction<VkExtensionProperties> func = (api, count, array) -> api.vkEnumerateInstanceExtensionProperties(null, count, array);
		return VulkanHelper.extensions(lib, func);
	}

	/**
	 * @param lib Vulkan library
	 * @return Validation layers supported by this platform
	 */
	static Set<ValidationLayer> layers(VulkanLibrary lib) {
		final VulkanFunction<VkLayerProperties> func = (api, count, array) -> api.vkEnumerateInstanceLayerProperties(count, array);
		return ValidationLayer.enumerate(lib, func);
	}

	/**
	 * Base-class Vulkan JNA structure.
	 * Note that this class <b>must</b> be defined as a member of the associated API in order for the type mapper to work correctly.
	 */
	abstract class VulkanStructure extends Structure {
		/**
		 * Constructor.
		 */
		protected VulkanStructure() {
			super(MAPPER);
		}

		@Override
		public List<Field> getFieldList() {
			return super.getFieldList();
		}

		@Override
		public Structure[] toArray(int size) {
			// TODO - hmmm didn't have to do this previously?
			if(size == 0) {
				return (Structure[]) Array.newInstance(getClass(), 0);
			}

			// Allocate array
			final Structure[] array = super.toArray(size);

			// Patch structure type field
			getFieldList()
					.stream()
					.filter(f -> f.getName().equals("sType"))
					.findAny()
					.ifPresent(f -> patch(f, array));

			return array;
		}

		/**
		 * Initialises the type field of all elements in the array.
		 */
		private void patch(Field sType, Structure[] array) {
			try {
				final Object value = sType.get(this);
				for(Structure struct : array) {
					sType.set(struct, value);
				}
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
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
