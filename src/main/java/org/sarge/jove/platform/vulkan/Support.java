package org.sarge.jove.platform.vulkan;

import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Set;

import com.sun.jna.Native;
import com.sun.jna.Structure;

/**
 * A <i>support</i> is a partial implementation helper class used to retrieve supporting features for a Vulkan implementation or device.
 * <p>
 * A concrete implementation should over-ride {@link #identity()} to generate the array identity element and {@link #map(Structure)} to convert a Vulkan structure to the output type.
 * <p>
 * These abstract methods are also intended to also be over-ridden when unit-testing.
 * <p>
 * Usage:
 * <pre>
 *  // Create support helper
 *  Support<SomeStructure, Result> support = new Support<>() {
 *      public SomeStructure identity() {
 *          return new SomeStructure();
 *      }
 *
 *      public Result map(SomeStructure struct) {
 *          return new Result(...);
 *      }
 *  };
 *
 *  // Get function
 *  VulkanFunction<SomeStructure> func = (api, count, array) -> api.vkEnumerateSomeStructureOrOther(...);
 *
 *  // Enumerate supporting features
 *  Set<Result> results = support.enumerate(func);
 * </pre>
 * @param <T> Structure type
 * @param <R> Result type
 * @see VulkanFunction
 * @author Sarge
 */
public abstract class Support<T extends Structure, R> {
	/**
	 * Retrieves a set of supporting features.
	 * @param lib			Vulkan library
	 * @param func			Enumeration function
	 * @return Results
	 */
	public Set<R> enumerate(VulkanLibrary lib, VulkanFunction<T> func) {
		final T[] array = VulkanFunction.enumerate(func, lib, identity());
		return Arrays.stream(array).map(this::map).collect(toSet());
	}

	/**
	 * Factory for the identity structure.
	 * @return New identity structure
	 */
	protected abstract T identity();

	/**
	 * Converts a retrieved structure to the resultant type.
	 * @param struct Structure
	 * @return Converted result
	 */
	protected abstract R map(T struct);

	/**
	 * Implementation for supported extensions.
	 */
	public static class Extensions extends Support<VkExtensionProperties, String> {
		@Override
		protected VkExtensionProperties identity() {
			return new VkExtensionProperties();
		}

		@Override
		protected String map(VkExtensionProperties struct) {
			return Native.toString(struct.extensionName);
		}
	}
}
