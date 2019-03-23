package org.sarge.jove.platform.vulkan;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.Feature.Supported;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * The <i>Vulkan</i> object is a singleton encapsulating global access to the Vulkan library and supporting features.
 * @author Sarge
 */
public class Vulkan {
	/**
	 * Factory for return-by-reference types.
	 */
	public interface ReferenceFactory {
		/**
		 * @return New pointer-by-reference
		 */
		PointerByReference reference();

		/**
		 * @return New integer-by-reference
		 */
		IntByReference integer();

		/**
		 * @return New pointer-array-by-reference
		 * @param size Array size
		 */
		Pointer[] pointers(int size);

		/**
		 * Default implementation.
		 */
		ReferenceFactory DEFAULT = new ReferenceFactory() {
			@Override
			public PointerByReference reference() {
				return new PointerByReference();
			}

			@Override
			public IntByReference integer() {
				return new IntByReference();
			}

			@Override
			public Pointer[] pointers(int size) {
				return new Pointer[size];
			}
		};
	}

	private static Vulkan instance;

	/**
	 * Initialises Vulkan.
	 * @return Vulkan singleton
	 */
	public static synchronized void init() {
		if(instance != null) throw new IllegalStateException("Vulkan instance has already been initialised");
		final VulkanLibrary lib = VulkanLibrary.create();
		instance = new Vulkan(lib, ReferenceFactory.DEFAULT);
	}

	/**
	 * Initialises Vulkan with a mock implementation.
	 * @param vulkan Vulkan singleton
	 */
	protected static void init(Vulkan vulkan) {
		instance = notNull(vulkan);
	}

	/**
	 * @return Vulkan singleton
	 */
	protected static Vulkan instance() {
		assert instance != null : "Vulkan has not been initialised";
		return instance;
	}

	private final VulkanLibrary lib;
	private final ReferenceFactory factory;

	private Supported supported;

	/**
	 * Constructor.
	 * @param lib 			Vulkan API
	 * @param factory		References factory
	 */
	protected Vulkan(VulkanLibrary lib, ReferenceFactory factory) {
		this.lib = notNull(lib);
		this.factory = notNull(factory);
	}

	/**
	 * @return Vulkan library
	 */
	public VulkanLibrary library() {
		return lib;
	}

	/**
	 * @return References factory
	 */
	public ReferenceFactory factory() {
		return factory;
	}

	/**
	 * Retrieves the supported Vulkan features.
	 * @return Supported features
	 */
	public synchronized Supported supported() {
		if(supported == null) {
			final VulkanFunction<VkExtensionProperties> extensions = (count, array) -> lib.vkEnumerateInstanceExtensionProperties(null, count, array);
			final VulkanFunction<VkLayerProperties> layers = (count, array) -> lib.vkEnumerateInstanceLayerProperties(count, array);
			supported = new Supported(extensions, layers);
		}

		return supported;
	}
}
