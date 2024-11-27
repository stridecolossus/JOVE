package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;

import java.util.function.IntFunction;

import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.*;

/**
 * The <i>Vulkan</i> service
 * TODO
 * @author Sarge
 */
public class Vulkan {
	/**
	 * Creates the Vulkan service.
	 * The success code of <b>all</b> native methods is validated by {@link #check(int)}.
	 * @return Vulkan
	 * @throws RuntimeException if Vulkan cannot be instantiated
	 */
	public static Vulkan create() {
		// Register Vulkan type mappers
		final var registry = TransformerRegistry.create();
//		registry.add(new StructureNativeMapper(registry));

		//////////
//		registry.add(new HandleArrayNativeMapper());
		//////////

		// Create API factory
		final var factory = new NativeFactory(registry);
		factory.setIntegerReturnHandler(Vulkan::check);

		// Instantiate API
		final var lib = factory.build("vulkan-1", VulkanLibrary.class);

		// Create wrapper
		return new Vulkan(lib, registry, new ReferenceFactory());
	}

	private final VulkanLibrary lib;
	private final TransformerRegistry registry;
	private final ReferenceFactory factory;

	/**
	 * Constructor.
	 * @param lib			Vulkan API
	 * @param registry		Mapper registry
	 * @param factory		Reference factory
	 */
	public Vulkan(VulkanLibrary lib, TransformerRegistry registry, ReferenceFactory factory) {
		this.lib = requireNonNull(lib);
		this.registry = requireNonNull(registry);
		this.factory = requireNonNull(factory);
	}

	/**
	 * @return Vulkan API
	 */
	public VulkanLibrary library() {
		return lib;
	}

	/**
	 * @return Vulkan mapper registry
	 */
	public TransformerRegistry registry() {
		return registry;
	}

	/**
	 * @return Reference factory
	 */
	public ReferenceFactory factory() {
		return factory;
	}

	/**
	 * Checks the result of a Vulkan API method.
	 * @param result Result code
	 * @throws VulkanException if the given result is not {@link VkResult#SUCCESS}
	 */
	public static void check(int result) throws VulkanException {
		if(result != VulkanLibrary.SUCCESS) {
			throw new VulkanException(result);
		}
	}

	/**
	 * @param size Buffer offset or size
	 * @throws IllegalArgumentException if the given size is not a multiple of 4 bytes
	 */
	public static void checkAlignment(long size) {
		if((size % 4) != 0) {
			throw new IllegalArgumentException("Expected 4-byte alignment");
		}
	}

	/**
	 * Invokes a Vulkan function using the <i>two stage invocation</i> approach.
	 * <p>
	 * This method is equivalent to the following:
	 * <pre>
	 * // Determine number of results
	 * var count = new IntegerReference();
	 * lib.someFunction(count, null);
	 *
	 * // Allocate container
	 * Handle[] array = new Handle[count.getValue()];
	 *
	 * // Populate container
	 * lib.someFunction(count, array);
	 * </pre>
	 * @param <T> Data type
	 * @param function		Vulkan function
	 * @param create		Creates the by-reference container
	 * @return Function result
	 */
	public <T> T invoke(VulkanFunction<T> function, IntFunction<T> create) {
		// Determine the result size
		final var count = factory.integer();
		function.enumerate(count, null);

		// Instantiate the container
		final int size = count.value();
		final T data = create.apply(size);

		// Invoke again to populate the container
		if(size > 0) {
			function.enumerate(count, data);
		}

		return data;
	}

	/**
	 * Enumerates the extensions supported by the given layer.
	 * @param name Layer name or {@code null} for extensions provided by the Vulkan implementation and any implicit layers
	 * @return Supported extensions
	 */
	public VkExtensionProperties[] extensions(String name) {
		final VulkanFunction<VkExtensionProperties[]> function = (count, array) -> lib.vkEnumerateInstanceExtensionProperties(name, count, array);
		return invoke(function, VkExtensionProperties[]::new);
	}

	/**
	 * Enumerates the validation layers supported by this Vulkan platform.
	 * @return Supported layers
	 */
	public VkLayerProperties[] layers() {
		final VulkanFunction<VkLayerProperties[]> function = (count, array) -> lib.vkEnumerateInstanceLayerProperties(count, array);
		return invoke(function, VkLayerProperties[]::new);
	}
}
