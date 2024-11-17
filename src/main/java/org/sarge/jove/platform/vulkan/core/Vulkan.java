package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.lib.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.VulkanException;

/**
 * The <i>Vulkan</i> service
 * TODO
 * @author Sarge
 */
public class Vulkan {
	/**
	 * Creates the Vulkan service.
	 * @return Vulkan
	 * @throws RuntimeException if Vulkan cannot be instantiated
	 */
	public static Vulkan create() {
		// Register Vulkan type mappers
		final var registry = NativeMapperRegistry.create();
//		registry.add(new StructureNativeMapper(registry));

		//////////
//		registry.add(new HandleArrayNativeMapper());
		//////////

		// Instantiate API
		final var factory = new NativeFactory(registry);
		final var lib = factory.build("vulkan-1", VulkanLibraryTEMP.class);

		// Create wrapper
		return new Vulkan(lib, registry, new ReferenceFactory());
	}

	private final VulkanLibraryTEMP lib;
	private final NativeMapperRegistry registry;
	private final ReferenceFactory factory;

	/**
	 * Constructor.
	 * @param lib			Vulkan API
	 * @param registry		Mapper registry
	 * @param factory		Reference factory
	 */
	public Vulkan(VulkanLibraryTEMP lib, NativeMapperRegistry registry, ReferenceFactory factory) {
		this.lib = requireNonNull(lib);
		this.registry = requireNonNull(registry);
		this.factory = requireNonNull(factory);
	}

	/**
	 * @return Vulkan API
	 */
	public VulkanLibraryTEMP library() {
		return lib;
	}

	/**
	 * @return Vulkan mapper registry
	 */
	public NativeMapperRegistry registry() {
		return registry;
	}

	/**
	 * @return Reference factory
	 */
	public ReferenceFactory factory() {
		return factory;
	}

	/**
	 * Enumerates the extensions supported by this Vulkan platform.
	 * @return Supported extensions
	 */
	public VkExtensionProperties extensions() {

		// lib.vkEnumerateInstanceExtensionProperties(null, count, null);

		// TODO
		return null;
	}

	/**
	 * Enumerates the validation layers supported by this Vulkan platform.
	 * @return Supported layers
	 */
	public VkLayerProperties layers() {
		// TODO
		return null;
	}

	/**
	 * Checks the result of a Vulkan API method.
	 * @param result Result code
	 * @throws VulkanException if the given result is not {@link VkResult#SUCCESS}
	 */
	public static void check(int result) throws VulkanException {
		if(result != VulkanLibraryTEMP.SUCCESS) {
			throw new VulkanException(result);
		}
	}
	// TODO - can we adapt the proxy thing to do this as well?
}
