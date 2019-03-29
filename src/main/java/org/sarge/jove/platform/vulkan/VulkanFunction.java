package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;

import java.lang.reflect.Array;
import java.util.function.IntFunction;

import org.sarge.jove.platform.vulkan.Vulkan.ReferenceFactory;

import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>vulkan function</i> is a method that retrieves an array from Vulkan using the <i>two invocations</i> approach using the same function.
 * <ol>
 * <li>retrieve the size of the array (array parameter is ignored)</li>
 * <li>retrieve the actual array</li>
 * </ol>
 * @param <T> Vulkan type
 */
public interface VulkanFunction<T> {
	/**
	 * Retrieves an array.
	 * @param count			Number of values
	 * @param results		Returned results
	 * @return Result code
	 */
	int enumerate(IntByReference count, T results);

	/**
	 * Retrieves a JNA structure array from Vulkan.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>the {@code obj} parameter is the <i>identity</i> structure used to generate the resultant array</li>
	 * <li>the returned array may be empty (but not <tt>null</tt>)</li>
	 * <li>result codes returned by Vulkan are also checked</li>
	 * </ul>
	 * <p>
	 * This method is essentially equivalent to the following:
	 * <pre>
	 * // Count number of results
	 * final ReferenceFactory factory = ...
	 * final IntByReference count = factory.integer();
	 * invoke(count, null);
	 *
	 * // Allocate the array
	 * final Structure identity = ...
	 * final int size = count.getValue();
	 * final Structure[] array = (Structure[]) identity.toArray(size);
	 *
	 * // Invoke again to retrieve the array
	 * invoke(count, array[0]);
	 * </pre>
	 * @param func		Vulkan function
	 * @param count		Returned count
	 * @param obj		Identity object
	 * @return Array
	 * @param <T> Structure type
	 * @see #enumerate(IntByReference, Object)
	 * @see ReferenceFactory#integer()
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Structure> T[] enumerate(VulkanFunction<T> func, IntByReference count, T obj) {
		// Count number of values
		check(func.enumerate(count, null));

		// Retrieve values
		if(count.getValue() > 0) {
			final T[] array = (T[]) obj.toArray(count.getValue());
			check(func.enumerate(count, array[0]));
			return array;
		}
		else {
			return (T[]) Array.newInstance(obj.getClass(), 0);
		}
	}

	/**
	 * Retrieves an arbitrary-typed array from Vulkan.
	 * @param func			Vulkan function
	 * @param count			Returned count
	 * @param factory		Allocates the array
	 * @return Array
	 * @param <T> Array component type
	 * @see #enumerate(IntByReference, Object)
	 * @see ReferenceFactory#integer()
	 */
	public static <T> T[] array(VulkanFunction<T[]> func, IntByReference count, IntFunction<T[]> factory) {
		// Count number of values
		check(func.enumerate(count, null));

		// Allocate array
		final T[] array = factory.apply(count.getValue());

		// Retrieve array
		if(array.length > 0) {
			check(func.enumerate(count, array));
		}

		return array;
	}
}
