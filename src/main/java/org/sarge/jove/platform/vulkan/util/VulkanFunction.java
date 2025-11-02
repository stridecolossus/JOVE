package org.sarge.jove.platform.vulkan.util;

import java.util.function.IntFunction;

import org.sarge.jove.foreign.IntegerReference;
import org.sarge.jove.platform.vulkan.VkResult;

/**
 * A <i>Vulkan function</i> abstracts a Vulkan API method used to retrieve data via the <i>two stage invocation</i> approach.
 * <p>
 * The function is of the form {@code function(IntegerReference count, T data)}
 * <p>
 * Where:
 * <ul>
 * <li><i>count</i> is the size of the data</li>
 * <li><i>data</i> is a pre-allocated <i>by reference</i> parameter populated by the function</li>
 * <li>the return value is either {@code void} or a {@link VkResult} success code</li>
 * </ul>
 * <p>
 * The method is invoked <b>twice</b> to retrieve the data from the native API method:
 * <ol>
 * <li>once to retrieve the size (or array length) of the result with the <i>data</i> argument set to {@code null}</li>
 * <li>and again to populate the provided container</li>
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
	 * <pre>
	 * // Given an API method that returns the size and data as by-reference parameters...
	 * interface Library {
	 *     void method(IntegerReference count, Handle[] handle);
	 * }
	 *
	 * // Determine the size of the container
	 * IntegerReference count = new IntegerReference();
	 * lib.method(count, null);
	 *
	 * // Allocate the container
	 * Handle[] array = new Handle[count.getValue()];
	 *
	 * // Invoke again to populate the container
	 * lib.method(count, array);
	 * </pre>
	 * @param <T> Data type
	 * @param function		Vulkan function
	 * @param supplier		Creates a container of the required size
	 * @return Results
	 */
	static <T> T invoke(VulkanFunction<T> function, IntFunction<T> supplier) {
		// Determine the size of the results
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
