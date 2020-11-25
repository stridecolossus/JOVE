package org.sarge.jove.platform.vulkan.api;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.VkLayerProperties;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;

import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.Library;
import com.sun.jna.Memory;
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
	 * Identifier for a Vulkan integration test.
	 */
	String INTEGRATION_TEST = "vulkan-integration-test";

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
		final String name = switch(Platform.getOSType()) {
			case Platform.WINDOWS -> "vulkan-1";
			case Platform.LINUX -> "libvulkan";
			default -> throw new UnsupportedOperationException("Unsupported platform: " + Platform.getOSType());
		};

		return Native.load(name, VulkanLibrary.class, Map.of(Library.OPTION_TYPE_MAPPER, MAPPER));
	}

	/**
	 * Base-class Vulkan JNA structure.
	 * Note that this class must be defined as a member of the associated API in order for the type mapper to work correctly.
	 */
	abstract class VulkanStructure extends Structure {
		protected VulkanStructure() {
			super(MAPPER);
		}

		/**
		 * Clones this structure.
		 * @param <T> Structure type
		 * @return New clone of this structure
		 */
		@SuppressWarnings("unchecked")
		public <T extends VulkanStructure> T copy() {
			// Sync with native
			write();

			// Copy memory from this structure
			final int size = this.size();
			final byte[] bytes = this.getPointer().getByteArray(0, size);

			// Create new memory block
            final Memory mem = new Memory(size);
            mem.write(0, bytes, 0, size);

            // Instantiate clone
            final var copy = newInstance(this.getClass(), mem);
            copy.read();

            // Cast to this structure type
            return (T) copy;
		}

		@Override
		public Structure[] toArray(int size) {
			final Structure[] array = super.toArray(size);
			patch(array);
			return array;
		}

		// JNA allocates an empty memory block for a new array, patch the required type field (if present)
		// This is pretty ropey, alternatives:
		// 1. Somehow identify VK structures with pre-populated fields?
		// 2. Re-implement to perform a memory copy of this object?
		private void patch(Structure[] array) {
			try {
				// Lookup type field (if present)
				final Field field = this.getClass().getField("sType");

				// Patch type to array elements
				final Object sType = field.get(this);
				for(Structure struct : array) {
					field.set(struct, sType);
				}
			}
			catch(IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			catch(NoSuchFieldException ignored) {
				// Ignored
			}
		}

		/**
		 * Helper - Allocates and populates an array of the given Vulkan structure as a contiguous memory block.
		 * @param <R> Structure type
		 * @param <T> Source data type
		 * @param ctor			Constructor
		 * @param data			Data
		 * @param populate		Population function
		 * @return New array or {@code null} is the source data is empty
		 */
		@SuppressWarnings("unchecked")
		public static <R extends Structure, T> R[] populateArray(Supplier<R> ctor, Collection<T> data, BiConsumer<T, R> populate) {
			// Check for empty data
			if(data.isEmpty()) {
				return null;
			}

			// Allocate array
			final R identity = ctor.get();
			final R[] array = (R[]) identity.toArray(data.size());

			// Populate array
			final Iterator<T> itr = data.iterator();
			for(int n = 0; n < array.length; ++n) {
				populate.accept(itr.next(), array[n]);
			}

			return array;
		}

		/**
		 * Helper - Allocates and populates an array of the given Vulkan structure as a contiguous memory block and returns the <b>first</b> element.
		 * @param <R> Structure type
		 * @param <T> Source data type
		 * @param ctor			Constructor
		 * @param data			Data
		 * @param populate		Population function
		 * @return <b>First</b> element of the new array or {@code null} if the source data is empty
		 */
		public static <R extends Structure, T> R populate(Supplier<R> ctor, Collection<T> data, BiConsumer<T, R> populate) {
			final R[] array = populateArray(ctor, data, populate);
			if(array == null) {
				return null;
			}
			else {
				return array[0];
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
