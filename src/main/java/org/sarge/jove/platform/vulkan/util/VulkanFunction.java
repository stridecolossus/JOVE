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
	 */
	void enumerate(IntegerReference count, T data);
}
