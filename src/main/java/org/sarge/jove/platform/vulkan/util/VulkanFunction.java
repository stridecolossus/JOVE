package org.sarge.jove.platform.vulkan.util;

import java.util.function.IntFunction;

import org.sarge.jove.foreign.NativeReference.IntegerReference;

/**
 * A <i>Vulkan function</i> abstracts an API method used to retrieve data from Vulkan via the <i>two stage invocation</i> approach.
 * <p>
 * The function is of the form:
 * <p>
 * <pre>int function(IntegerReference count, T data)</pre>
 * Where:
 * <ul>
 * <li><i>count</i> is the size of the data</li>
 * <li><i>data</i> is a pre-allocated container (often an array) to be populated by the function</li>
 * <li>the return value is a Vulkan success code</li>
 * </ul>
 * <p>
 * The method is invoked <b>twice</b> to retrieve the data from the native API method:
 * <ol>
 * <li>retrieve the size (or array length) of the data (the <i>data</i> argument is {@code null})</li>
 * <li>invoke again to populate the data as a by-reference argument</li>
 * </ol>
 * @param <T> Data type
 * @author Sarge
 */
@FunctionalInterface
public interface VulkanFunction<T> {
	/**
	 * Vulkan API method that retrieves data using the <i>two stage invocation</i> approach.
	 * @param count 		Size of the data (or array length)
	 * @param data			Returned data or {@code null} to retrieve the size of the data
	 */
	void invoke(IntegerReference count, T data);

	/**
	 * Invokes the given function using the <i>two stage invocation</i> approach.
	 * <p>
	 * This method is equivalent to the following:
	 *
	 * TODO
	 *
	 * <pre>
	 *
	 * interface Library {
	 *     void method(IntegerReference count, Handle[] handle);
	 * }
	 *
	 * // Determine number of results
	 * IntegerReference count = new IntegerReference();
	 * lib.method(count, null);
	 *
	 * // Allocate container
	 * Handle[] array = new Handle[count.getValue()];
	 *
	 * // Populate container
	 * lib.method(count, array);
	 * </pre>
	 * @param <T> Data type
	 * @param function		Vulkan function
	 * @param supplier		Creates a container of the required size
	 * @return Function result
	 */
	static <T> T invoke(VulkanFunction<T> function, IntFunction<T> supplier) {
		// Determine the result size
		//final NativeReference<Integer> count = factory.integer();
		final var count = new IntegerReference();
		function.invoke(count, null);

		// Instantiate the container
		final int size = count.get();
		final T data = supplier.apply(size);

		// Invoke again to populate the container
		if(size > 0) {
			function.invoke(count, data);
		}

		return data;
	}
}
