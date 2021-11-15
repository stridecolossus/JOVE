package org.sarge.jove.platform.vulkan.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.sarge.jove.common.Handle;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.image.ImageLibrary;
import org.sarge.jove.platform.vulkan.memory.MemoryLibrary;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLibrary;
import org.sarge.jove.platform.vulkan.render.RenderLibrary;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.IntegerEnumeration;

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
		final DefaultTypeMapper mapper = new DefaultTypeMapper();
		mapper.addTypeConverter(VulkanBoolean.class, VulkanBoolean.CONVERTER);
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
	 * @throws VulkanException if the given result is not {@link VkResult#SUCCESS}
	 */
	static void check(int result) {
		if(result != SUCCESS) {
			throw new VulkanException(result);
		}
	}
}

interface GraphicsLibrary extends PipelineLibrary, ImageLibrary, RenderLibrary, Surface.Library {
	// Aggregate library
}

interface DeviceLibrary extends Instance.Library, PhysicalDevice.Library, LogicalDevice.Library {
	// Aggregate library
}

interface UtilityLibrary extends VulkanBuffer.Library, Command.Library, Semaphore.Library, Fence.Library {
	// Aggregate library
}
