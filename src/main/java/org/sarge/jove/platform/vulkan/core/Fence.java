package org.sarge.jove.platform.vulkan.core;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * A <i>fence</i> is used to synchronise between application code and Vulkan.
 * @author Sarge
 */
public class Fence extends VulkanObject {
	private static final ReverseMapping<VkResult> MAPPING = ReverseMapping.mapping(VkResult.class);

	/**
	 * Creates a fence.
	 * @param device		Logical device
	 * @param flags			Creation flags
	 * @return Fence
	 */
	public static Fence create(LogicalDevice device, VkFenceCreateFlag... flags) {
		// Init descriptor
		final var info = new VkFenceCreateInfo();
		info.flags = new EnumMask<>(flags);

		// Create fence
		final Library library = device.library();
		final var handle = new Pointer();
		library.vkCreateFence(device, info, null, handle);

		// Create domain object
		return new Fence(handle.get(), device);
	}

	private final Library library;

	/**
	 * Constructor.
	 * @param handle		Fence handle
	 * @param device		Logical device
	 */
	protected Fence(Handle handle, LogicalDevice device) {
		super(handle, device);
		this.library = device.library();
	}

	/**
	 * @return Whether this fence has been signalled
	 */
	public boolean signalled() {
		final int code = library.vkGetFenceStatus(this.device(), this);
		final VkResult result = MAPPING.map(code);
		return switch(result) {
			case SUCCESS	-> true;
			case NOT_READY	-> false;
			default			-> throw new VulkanException(result);
		};
	}

	/**
	 * Resets this fence.
	 * @see #reset(LogicalDevice, Collection)
	 */
	public void reset() {
		reset(Set.of(this));
	}

	/**
	 * Resets a group of fences.
	 * @param fences Fences to reset
	 */
	public static void reset(Collection<Fence> fences) {
		if(fences.isEmpty()) {
			return;
		}
		final Fence[] array = fences.toArray(Fence[]::new);
		final LogicalDevice device = array[0].device();
		final Library library = array[0].library;
		library.vkResetFences(device, array.length, array);
	}

	/**
	 * Blocks until this fence is ready.
	 * @see #waitReady(Collection, boolean, long)
	 */
	public void waitReady() {
		waitReady(Set.of(this), true, Long.MAX_VALUE);
	}

	/**
	 * Waits for a group of fences.
	 * @param device		Logical device
	 * @param fences		Fences
	 * @param all			Whether to wait for all or any fence
	 * @param timeout		Timeout (nanoseconds)
	 */
	public static void waitReady(Collection<Fence> fences, boolean all, long timeout) {
		if(fences.isEmpty()) {
			return;
		}
		final Fence[] array = fences.toArray(Fence[]::new);
		final LogicalDevice device = array[0].device();
		final Library library = array[0].library;
		library.vkWaitForFences(device, array.length, array, all, timeout);
	}

	@Override
	protected Destructor<Fence> destructor() {
		return library::vkDestroyFence;
	}

	/**
	 * Fence API.
	 */
	interface Library {
		/**
		 * Creates a fence.
		 * @param device			Device
		 * @param pCreateInfo		Descriptor
		 * @param pAllocator		Allocator
		 * @param pFence			Returned fence
		 * @return Result
		 */
		VkResult vkCreateFence(LogicalDevice device, VkFenceCreateInfo pCreateInfo, Handle pAllocator, Pointer pFence);

		/**
		 * Destroys a fence.
		 * @param device			Device
		 * @param fence				Fence
		 * @param pAllocator		Allocator
		 */
		void vkDestroyFence(LogicalDevice device, Fence fence, Handle pAllocator);

		/**
		 * Resets a number of fences.
		 * @param device			Device
		 * @param fenceCount		Number of fences
		 * @param pFences			Fences
		 * @return Result
		 */
		VkResult vkResetFences(LogicalDevice device, int fenceCount, Fence[] pFences);

		/**
		 * Retrieves the status of a given fence.
		 * Note that this method returns a {@link VkResult} status code.
		 * @param device
		 * @param fence
		 * @return Fence status flag
		 * @implNote Returns {@code int} since this method has multiple success codes
		 */
		int vkGetFenceStatus(LogicalDevice device, Fence fence);

		/**
		 * Waits for a number of fences.
		 * @param device			Device
		 * @param fenceCount		Number of fences
		 * @param pFences			Fences
		 * @param waitAll			Whether to wait for <b>all</b> or <b>any</b> fence
		 * @param timeout			Timeout or {@link Long#MAX_VALUE} (nanoseconds)
		 * @return Result
		 */
		VkResult vkWaitForFences(LogicalDevice device, int fenceCount, Fence[] pFences, boolean waitAll, long timeout);
	}
}
