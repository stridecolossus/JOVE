package org.sarge.jove.platform.vulkan.util;

import org.sarge.jove.foreign.IntegerReference;

/**
 * A <i>Vulkan function</i> abstracts an API method used to retrieve data from Vulkan via the <i>two-stage invocation</i> approach.
 * <p>
 * The function is of the form:
 * <p>
 * <pre>int function(IntByReference count, T data)</pre>
 * Where:
 * <ul>
 * <li><i>count</i> is the size of the data</li>
 * <li><i>data</i> is a pre-allocated container (often an array) to be populated by the function</li>
 * <li>the return value is a Vulkan success code</li>
 * </ul>
 * <p>
 * The method is invoked <b>twice</b> to retrieve the data from the native API method:
 * <ol>
 * <li>retrieve the length of the data (the <i>data</i> argument is {@code null})</li>
 * <li>populate the data (passing back the <i>count</i>)</li>
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
	int enumerate(IntegerReference count, T data);
}

//
//	/**
//	 * Invokes a Vulkan function using the <i>two-stage invocation</i> approach.
//	 * <p>
//	 * Example to retrieve an array of pointers:
//	 * <pre>
//	 * VulkanLibrary lib = ...
//	 * VulkanFunction&lt;Pointer[]&gt; func = (count, array) -> lib.someFunction(count, array);
//	 * Pointer[] array = func.invoke(new IntegerByReference(), Pointer[]::new);
//	 * </pre>
//	 * This method is equivalent to the following:
//	 * <pre>
//	 * lib.someFunction(count, null);
//	 * Pointer[] array = new Pointer[count.getValue()];
//	 * lib.someFunction(count, array);
//	 * </pre>
//	 * @param <T> Data type
//	 * @param function		Vulkan function
//	 * @param count			Size of the data
//	 * @param factory		Creates the resultant data object
//	 * @return Function result
//	 */
//	static <T> T invoke(VulkanFunction<T> function, IntegerReference count, IntFunction<T> factory) {
//		// Invoke to determine the size of the data
//		function.enumerate(count, null);
//
//		// Instantiate the data object
//		final int size = count.value();
//		final T data = factory.apply(size);
//
//		// Invoke again to populate the data object
//		if(size > 0) {
//			function.enumerate(count, data);
//		}
//
//		return data;
//	}
//}

//
//	/**
//	 * Invokes a Vulkan function using the <i>two-stage invocation</i> approach to retrieve an array of JNA structures.
//	 * <p>
//	 * Note that a JNA structure array <b>must</b> be a contiguous block of memory allocated via the {@link Structure#toArray(int)} helper.
//	 * <p>
//	 * Usage:
//	 * <pre>
//	 * VulkanLibrary lib = ...
//	 * VulkanFunction&lt;SomeStructure&gt; func = (count, array) -> lib.someFunction(count, array);
//	 * SomeStructure[] array = func.invoke(new IntegerByReference(), SomeStructure::new);
//	 * </pre>
//	 * This adapter is equivalent to the following:
//	 * <pre>
//	 * lib.someFunction(count, null);
//	 * SomeStructure[] array = (SomeStructure[]) new SomeStructure().toArray(count.getValue());
//	 * lib.someFunction(count, array[0]);
//	 * </pre>
//	 * <p>
//	 * @param <T> Structure type
//	 * @param function		Vulkan function
//	 * @param count			Array size
//	 * @param identity		Identity structure
//	 * @return JNA structure array
//	 */
//	static <T extends NativeStructure> T[] invoke(VulkanFunction<T> function, IntegerReference count, T identity) {
//		// Invoke to determine the length of the array
//		check(function.enumerate(count, null));
//
//		// Instantiate the structure array
//		@SuppressWarnings("unchecked")
//		//final T[] array = (T[]) identity.toArray(count.value());
//		final T[] array = null;
//
//		// Invoke again to populate the array (note passes first element)
//		if(array.length > 0) {
//			check(function.enumerate(count, array[0]));
//		}
//
//		return array;
//	}
//}
