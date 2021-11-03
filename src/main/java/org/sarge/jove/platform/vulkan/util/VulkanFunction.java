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
 * <li><i>count</i> is the length of the array</li>
 * <li><i>array</i> is a pre-allocated array to be populated by the function or {@code null} to retrieve the length of the array</li>
 * <li>the return value is a Vulkan success code</li>
 * </ul>
 * <p>
 * The method is invoked <b>twice</b> to retrieve the data from the native API method:
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
	 * @param lib			Vulkan library
	 * @param count 		Size of the data
	 * @param data			Returned data or {@code null} to retrieve the size of the data
	 * @return Vulkan result code
	 */
	int enumerate(VulkanLibrary lib, IntByReference count, T data);

	/**
	 * Invokes a Vulkan function that uses the <i>two-stage invocation</i> approach.
	 * @param <T> Data type
	 * @param func			Vulkan function
	 * @param lib			API
	 * @param factory		Creates the resultant data object
	 * @return Resultant data
	 */
	static <T> T invoke(VulkanFunction<T> func, VulkanLibrary lib, IntFunction<T> factory) {
		// Invoke to determine the size of the data
		final IntByReference count = lib.factory().integer();
		check(func.enumerate(lib, count, null));

		// Instantiate the data object
		final int size = count.getValue();
		final T data = factory.apply(size);

		// Invoke again to populate the data object
		if(size > 0) {
			check(func.enumerate(lib, count, data));
		}

		return data;
	}

	/**
	 * Invokes a Vulkan function that uses the <i>two-stage invocation</i> approach to retrieve a JNA structure array.
	 * <p>
	 * Usage:
	 * <pre>
	 *  VulkanFunction&gt;SomeStructure&lt; func = (api, count, array) -> api.someFunction(count, array, ...);
	 *  SomeStructure[] array = VulkanFunction.enumerate(func, lib, SomeStructure::new);
	 * </pre>
	 * The adapter is equivalent to the following:
	 * <pre>
	 *  // Count number of results
	 *  api.someFunction(count, null, ...);
	 *
	 *  // Allocate JNA array
	 *  SomeStructure[] array = (SomeStructure[]) new SomeStructure().toArray(count.getValue());
	 *
	 *  // Populate array (note passes first element)
	 *  api.someFunction(count, array[0], ...);
	 * </pre>
	 * <p>
	 * @param <T> Structure type
	 * @param func			Vulkan function
	 * @param lib			API
	 * @param identity		Identity structure
	 * @return JNA structure array
	 */
	static <T extends Structure> T[] invoke(VulkanFunction<T> func, VulkanLibrary lib, Supplier<T> identity) {
		// Invoke to determine the length of the array
		final IntByReference count = lib.factory().integer();
		check(func.enumerate(lib, count, null));

		// Instantiate the structure array
		@SuppressWarnings("unchecked")
		final T[] array = (T[]) identity.get().toArray(count.getValue());

		// Invoke again to populate the array (note passes first element)
		if(array.length > 0) {
			check(func.enumerate(lib, count, array[0]));
		}

		return array;
	}
}
