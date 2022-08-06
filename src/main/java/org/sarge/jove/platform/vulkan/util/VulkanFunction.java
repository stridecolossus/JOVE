package org.sarge.jove.platform.vulkan.util;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;

import java.util.function.*;

import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>Vulkan function</i> abstracts an API method used to retrieve data from Vulkan via the <i>two-stage invocation</i> approach.
 * <p>
 * The function is of the following form:
 * <p>
 * <pre>  int function(IntByReference count, T data)</pre>
 * where:
 * <ul>
 * <li><i>count</i> is the size of the data</li>
 * <li><i>data</i> is a pre-allocated container (often an array) to be populated by the function</li>
 * <li>the return value is a Vulkan success code</li>
 * </ul>
 * <p>
 * The method is invoked <b>twice</b> to retrieve the data from the native API method:
 * <ol>
 * <li>retrieve the length of the data (the <i>data</i> argument is {@code null})</li>
 * <li>populate the data (passing back <i>count</i>)</li>
 * </ol>
 * @param <T> Data type
 * @author Sarge
 */
@FunctionalInterface
public interface VulkanFunction<T> {
	/**
	 * Vulkan API method that retrieves data using the <i>two-stage invocation</i> approach.
	 * @param count 		Size of the data
	 * @param data			Returned data or {@code null} to retrieve the size of the data
	 * @return Vulkan result code
	 */
	int enumerate(IntByReference count, T data);

	/**
	 * Invokes a Vulkan function that uses the <i>two-stage invocation</i> approach.
	 * <p>
	 * Example to retrieve an array of pointers:
	 * <pre>
	 * VulkanLibrary lib = ...
	 * VulkanFunction&lt;Pointer[]&gt; func = (count, array) -> lib.someFunction(count, array, ...);
	 * Pointer[] array = VulkanFunction.enumerate(func, new IntegerByReference(), Pointer[]::new);</pre>
	 * This method is equivalent to the following:
	 * <pre>
	 * // Count number of results
	 * IntegerByReference count = new IntegerByReference();
	 * lib.someFunction(count, null, ...);
	 *
	 * // Allocate data
	 * Pointer[] array = new Pointer[count.getValue()];
	 *
	 * // Populate array
	 * lib.someFunction(count, array, ...);</pre>
	 * @param <T> Data type
	 * @param func			Vulkan function
	 * @param count			Count
	 * @param factory		Creates the resultant data object
	 * @return Resultant data
	 */
	default T invoke(IntByReference count, IntFunction<T> factory) {
		// Invoke to determine the size of the data
		check(enumerate(count, null));

		// Instantiate the data object
		final int size = count.getValue();
		final T data = factory.apply(size);

		// Invoke again to populate the data object
		if(size > 0) {
			check(enumerate(count, data));
		}

		return data;
	}

	/**
	 * Vulkan function that retrieves an array of JNA structures.
	 * @param <T> Vulkan structure
	 */
	interface StructureVulkanFunction<T extends Structure> extends VulkanFunction<T> {
		/**
		 * Invokes a Vulkan function that uses the <i>two-stage invocation</i> approach to retrieve an <b>array</b> of JNA structures.
		 * <p>
		 * Note that a JNA structure array <b>must</b> be a contiguous block of memory allocated via the {@link Structure#toArray(int)} helper.
		 * <p>
		 * Usage:
		 * <pre>
		 * VulkanLibrary lib = ...
		 * VulkanFunction&lt;SomeStructure&gt; func = (count, array) -> lib.someFunction(count, array, ...);
		 * SomeStructure[] array = VulkanFunction.enumerate(func, new IntegerByReference(), SomeStructure::new);</pre>
		 * This adapter is equivalent to the following:
		 * <pre>
		 * // Count number of results
		 * IntegerByReference count = new IntegerByReference();
		 * lib.someFunction(count, null, ...);
		 *
		 * // Allocate JNA array
		 * SomeStructure[] array = (SomeStructure[]) new SomeStructure().toArray(count.getValue());
		 *
		 * // Populate array (note passes first element)
		 * lib.someFunction(count, array[0], ...);</pre>
		 * <p>
		 * @param <T> Structure type
		 * @param func			Vulkan function
		 * @param count			Count
		 * @param identity		Identity structure
		 * @return JNA structure array
		 */
		default T[] invoke(IntByReference count, Supplier<T> identity) {
			// Invoke to determine the length of the array
			check(enumerate(count, null));

			// Instantiate the structure array
			@SuppressWarnings("unchecked")
			final T[] array = (T[]) identity.get().toArray(count.getValue());

			// Invoke again to populate the array (note passes first element)
			if(array.length > 0) {
				check(enumerate(count, array[0]));
			}

			return array;
		}
	}
}
