package org.sarge.jove.platform.vulkan;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.Feature.Supported;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * The <i>Vulkan</i> context encapsulates global access to the Vulkan library and supporting features.
 * <p>
 * Usage:
 * <pre>
 * // Production code
 * Vulkan vulkan = Vulkan.create();
 *
 * // Test code
 * Vulkan vulkan = new Vulkan.create(...);
 * </pre>
 * @author Sarge
 */
public class Vulkan {
	/**
	 * The <i>reference factory</i> creates return-by-reference types used when invoking the Vulkan API.
	 * <p>
	 * The purpose of the reference factory is to centralise instantiation of return-by-reference types to support unit-testing.
	 * Test code should implement a factory that gives unit-tests access to the generated references (which is virtually impossible to mock otherwise).
	 * <p>
	 * The {@link #DEFAULT} production-code implementation simply generates <i>new</i> references.
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

	private static boolean created;

	/**
	 * Creates the global Vulkan context.
	 * @return Vulkan context
	 * @throws IllegalStateException if the context has already been created
	 */
	public static synchronized Vulkan create() {
		if(created) throw new IllegalStateException("Vulkan has already been created");
		created = true;
		return new Vulkan(VulkanLibrary.create(), ReferenceFactory.DEFAULT);
	}

	private final VulkanLibrary lib;
	private final ReferenceFactory factory;

	private Supported supported;

	/**
	 * Constructor.
	 * @param lib 			Vulkan API
	 * @param factory		Reference factory
	 */
	private Vulkan(VulkanLibrary lib, ReferenceFactory factory) {
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
	 * @return Reference factory
	 */
	public ReferenceFactory factory() {
		return factory;
	}

	/**
	 * Retrieves the global supported Vulkan features.
	 * @return Supported features
	 */
	public synchronized Supported supported() {
		if(supported == null) {
			final VulkanFunction<VkExtensionProperties> extensions = (count, array) -> lib.vkEnumerateInstanceExtensionProperties(null, count, array);
			final VulkanFunction<VkLayerProperties> layers = (count, array) -> lib.vkEnumerateInstanceLayerProperties(count, array);
			supported = new Supported(extensions, layers, factory);
		}

		return supported;
	}
}
