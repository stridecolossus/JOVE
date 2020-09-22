package org.sarge.jove.platform.vulkan;

import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Set;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>support</i> is an adapter for a {@link VulkanFunction} used to retrieve supported extensions and validation layers.
 * <p>
 * This is a skeleton base-class helper, see the {@link Extensions} helper class for an example implementation.
 * <p>
 * @param <T> Structure type
 * @param <R> Resultant type
 * @see VulkanFunction
 * @author Sarge
 */
public abstract class Support<T extends Structure, R> {
	/**
	 * Enumerates a set of supported data.
	 * @param func Underlying API function
	 * @return Results
	 */
	public abstract Set<R> enumerate(VulkanFunction<T> func);

	/**
	 * Enumerates and maps a set of supported data.
	 * @param func			Underlying API function
	 * @param count			Counter
	 * @param identity		Identity instance
	 * @return Results
	 */
	protected Set<R> enumerate(VulkanFunction<T> func, IntByReference count, T identity) {
		// Create function adapter
		final T[] array = VulkanFunction.enumerate(func, count, identity);

		// Enumerate results and convert
		return Arrays
				.stream(array)
				.map(this::map)
				.collect(toSet());
	}

	/**
	 * Transforms a structure to the resultant type.
	 * @param obj Structure
	 * @return Transformed result
	 */
	protected abstract R map(T obj);

	/**
	 * Implementation for supporting extensions.
	 */
	public static class Extensions extends Support<VkExtensionProperties, String> {
		@Override
		public Set<String> enumerate(VulkanFunction<VkExtensionProperties> func) {
			return enumerate(func, new IntByReference(), new VkExtensionProperties());
		}

		@Override
		protected String map(VkExtensionProperties ext) {
			return Native.toString(ext.extensionName);
		}
	}
}
