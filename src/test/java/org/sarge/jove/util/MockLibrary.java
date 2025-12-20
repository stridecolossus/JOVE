package org.sarge.jove.util;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.VkResult;

/**
 * Helper base-class to initialise by-reference parameters.
 * @author Sarge
 */
public abstract class MockLibrary {
	private int handle = 1;
	public VkResult result = VkResult.VK_SUCCESS;

	/**
	 * Initialises a returned pointer.
	 * @param pointer Pointer to initialise
	 */
	protected void init(Pointer pointer) {
		pointer.set(MemorySegment.ofAddress(handle++));
	}

	/**
	 * Initialises a return array of handles.
	 * @param handles Handles array
	 */
	protected void init(Handle[] handles) {
		if(handles != null) {
			Arrays.setAll(handles, _ -> new Handle(handle++));
		}
	}

	/**
	 * Initialises a by-reference array.
	 * Ignored if the array is {@code null}.
	 * @param <T> Component type
	 * @param array			Array
	 * @param instance		Element instance
	 */
	protected <T> void init(T[] array, T instance) {
		if(array == null) {
			return;
		}
		Arrays.fill(array, instance);
	}
}
