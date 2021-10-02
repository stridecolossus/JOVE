package org.sarge.jove.platform.vulkan.util;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;

import java.util.function.IntFunction;
import java.util.function.Supplier;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary;

import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>vulkan function</i> abstracts an API method used to retrieve a Vulkan array via the <i>two-stage invocation</i> approach.
 * <p>
 * The function is of the following form:
 * <p>
 * <code>int function(VulkanLibrary lib, IntByReference count, T[] array)</code>
 * <p>
 * where:
 * <ul>
 * <li><i>count</i> is a return-by-reference integer to retrieve/provide the length of the array</li>
 * <li><i>array</i> is a pre-allocated array to be populated by the function ({@code null} to retrieve the length of the array)</li>
 * <li>the return value is a Vulkan success code</li>
 * </ul>
 * <p>
 * The method is invoked <b>twice</b> to retrieve the array from the native API method:
 * <ol>
 * <li>retrieve the length of the array (array parameter is ignored)</li>
 * <li>populate the array (passing back the length)</li>
 * </ol>
 * @param <T> Array component type
 * @author Sarge
 */
@FunctionalInterface
public interface VulkanFunction<T> {
	/**
	 * Vulkan API method that retrieves an array of the given type using the <i>two-stage invocation</i> approach.
	 * @param lib		Vulkan library
	 * @param count 	Return-by-reference count of the number of array elements
	 * @param array 	Array instance or {@code null} to retrieve the size of the array
	 * @return Vulkan result code
	 */
	int enumerate(VulkanLibrary lib, IntByReference count, T array);

	/**
	 * Adapter for a function that retrieves an arbitrarily typed array using the <i>two-stage invocation</i> approach.
	 * @param <T> Array type
	 * @param func			Underlying function
	 * @param lib			Vulkan API
	 * @param factory		Array factory
	 * @return Populated array (can be zero length)
	 * @see #enumerate(VulkanLibrary, IntByReference, Object)
	 */
	static <T> T[] enumerate(VulkanFunction<T[]> func, VulkanLibrary lib, IntFunction<T[]> factory) {
		// Determine array length
		final IntByReference count = lib.factory().integer();
		check(func.enumerate(lib, count, null));

		// Allocate array
		final T[] array = factory.apply(count.getValue());

		// Retrieve array
		if(array.length > 0) {
			check(func.enumerate(lib, count, array));
		}

		return array;
	}

	/**
	 * Adapter for a function that retrieves an array of JNA structures using the <i>two-stage invocation</i> approach.
	 * <p>
	 * Usage:
	 * <pre>
	 *  VulkanFunction&gt;SomeStructure&lt; func = (api, count, array) -> api.someFunction(count, array, ...);
	 *  SomeStructure[] array = VulkanFunction.enumerate(func, vulkan, SomeStructure::new);
	 * </pre>
	 * The adapter is equivalent to the following:
	 * <pre>
	 *  // Count number of results
	 *  api.someFunction(count, null, ...);
	 *
	 *  // Allocate JNA array
	 *  SomeStructure[] array = (SomeStructure[]) new SomeStructure().toArray(count.getValue());
	 *
	 *  // Populate array
	 *  api.someFunction(count, array[0], ...);
	 * </pre>
	 * <p>
	 * @param <T> Structure type
	 * @param func			Underlying function
	 * @param lib			Vulkan API
	 * @param identity		Identity function
	 * @return Array of structures (can be zero length)
	 * @throws VulkanException if the underlying API method fails
	 * @see #enumerate(VulkanLibrary, IntByReference, Object)
	 */
	static <T extends Structure> T[] enumerate(VulkanFunction<T> func, VulkanLibrary lib, Supplier<T> identity) {
		// Determine array length
		final IntByReference count = lib.factory().integer();
		check(func.enumerate(lib, count, null));

		// Allocate array
		@SuppressWarnings("unchecked")
		final T[] array = (T[]) identity.get().toArray(count.getValue());

		// Retrieve array
		if(array.length > 0) {
			check(func.enumerate(lib, count, array[0]));
		}

		return array;
	}
}
