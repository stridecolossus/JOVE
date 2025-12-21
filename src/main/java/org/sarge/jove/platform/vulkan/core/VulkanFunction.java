package org.sarge.jove.platform.vulkan.core;

import java.util.function.IntFunction;

import org.sarge.jove.foreign.IntegerReference;

/**
 * A <i>Vulkan function</i> abstracts an API method that is used to retrieve data via the <i>two stage invocation</i> approach.
 * <p>
 * The function is of the form {@code function(IntegerReference count, T data)}
 * <p>
 * Where:
 * <ul>
 * <li><i>count</i> is the size of the data</li>
 * <li><i>data</i> is a pre-allocated <i>by reference</i> parameter populated by the function (usually an array)</li>
 * </ul>
 * <p>
 * The function is invoked <b>twice</b> to retrieve the data from the native API method:
 * <ol>
 * <li>once to retrieve the size of the result, where the <i>data</i> argument is set to {@code null}</li>
 * <li>and again to populate the provided container or array</li>
 * </ol>
 * <p>
 * The convenience {@link #invoke(VulkanFunction, IntFunction)} helper encapsulates the two-stage invocation process for a given function.
 * <p>
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
	 * Example: Given an API method that returns a by-reference array of handles:
	 * <pre>
	 * interface Library {
	 *     void method(IntegerReference count, @Updated Handle[] handle);
	 * }
	 * </pre>
	 * <p>
	 * the method can be invoked as follows:
	 * <pre>
	 * Handle[] array = VulkanFunction.invoke(library::method, Handle[]::new);
	 * </pre>
	 * <p>
	 * which is equivalent to:
	 * </pre>
	 * // Determine the size of the container
	 * IntegerReference count = new IntegerReference();
	 * library.method(count, null);
	 *
	 * // Allocate the container
	 * Handle[] array = new Handle[count.getValue()];
	 *
	 * // Invoke again to populate the container
	 * library.method(count, array);
	 * </pre>
	 * <p>
	 * @param <T> Data type
	 * @param function		Vulkan function
	 * @param factory		Factory for a container or array of the required size
	 * @return Results
	 */
	static <T> T invoke(VulkanFunction<T> function, IntFunction<T> factory) {
		// Determine the size of the results
		final var count = new IntegerReference();
		function.invoke(count, null);

		// Instantiate the container
		final int size = count.get();
		final T data = factory.apply(size);

		// Invoke again to populate the container
		if(size > 0) {
			function.invoke(count, data);
		}

		return data;
	}
}
