package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;

import java.lang.reflect.Array;
import java.util.function.IntFunction;

import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>vulkan function</i> abstracts an API method used to retrieve a Vulkan array via the <i>two invocations</i> approach.
 * <p>
 * The API method is invoked <b>twice</b>:
 * <ol>
 * <li>retrieve the size of the array (array parameter is ignored)</li>
 * <li>retrieve the actual array</li>
 * </ol>
 * @param <T> Vulkan type
 */
@FunctionalInterface
public interface VulkanFunction<T> {
	/**
	 * Vulkan API method that retrieves an array of the given type.
	 * @param api		Vulkan API
	 * @param count 	Return-by-reference count of the number of array elements
	 * @param array 	Array instance or <code>null</code> to retrieve size of the array
	 * @return Vulkan result code
	 */
	int enumerate(VulkanLibrary api, IntByReference count, T array);

	/**
	 * Adapter for a function that enumerates an array.
	 * @param <T> Array type
	 * @param func			Underlying function
	 * @param vulkan		Vulkan
	 * @param factory		Array factory
	 * @return Populated array (can be zero length)
	 * @see #enumerate(VulkanLibrary, IntByReference, Object)
	 */
	static <T> T[] enumerate(VulkanFunction<T[]> func, Vulkan vulkan, IntFunction<T[]> factory) {
		// Determine array length
		final VulkanLibrary api = vulkan.api();
		final IntByReference count = vulkan.integer();
		check(func.enumerate(api, count, null));

		// Allocate array
		final T[] array = factory.apply(count.getValue());

		// Retrieve array
		if(array.length > 0) {
			check(func.enumerate(api, count, array));
		}

		return array;
	}

	/**
	 * Adapter for a function that enumerates an array of JNA structures.
	 * <p>
	 * Usage:
	 * <pre>
	 *  VulkanFunction<Structure> func = (api, count, array) -> api.someFunction(count, array, ...);
	 *  Structure[] array = VulkanFunction.enumerate(func, vulkan, new Structure());
	 * </pre>
	 * The adapter is equivalent to the following:
	 * <pre>
	 *  // Count number of results
	 *  api.someFunction(count, null, ...);
	 *
	 *  // Allocate JNA array
	 *  Structure[] array = new Structure().toArray(count.getValue());
	 *
	 *  // Populate array
	 *  api.someFunction(count, array[0], ...);
	 * </pre>
	 * <p>
	 * @param <T> Structure type
	 * @param func			Underlying function
	 * @param vulkan		Vulkan
	 * @param identity		Identity instance
	 * @return Array of structures (can be zero length)
	 * @throws VulkanException if the underlying API method fails
	 * @see #enumerate(VulkanLibrary, IntByReference, Object)
	 */
	@SuppressWarnings("unchecked")
	static <T extends Structure> T[] enumerate(VulkanFunction<T> func, Vulkan vulkan, T identity) {
		// Count number of values
		final VulkanLibrary api = vulkan.api();
		final IntByReference count = vulkan.integer();
		check(func.enumerate(api, count, null));

		// Retrieve values
		if(count.getValue() > 0) {
			final T[] array = (T[]) identity.toArray(count.getValue());
			check(func.enumerate(api, count, array[0]));
			return array;
		}
		else {
			return (T[]) Array.newInstance(identity.getClass(), 0);
		}
	}
}
