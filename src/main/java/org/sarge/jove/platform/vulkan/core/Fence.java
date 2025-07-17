package org.sarge.jove.platform.vulkan.core;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeReference;
import org.sarge.jove.foreign.NativeReference.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>fence</i> is used to synchronise between application code and Vulkan.
 * @author Sarge
 */
public class Fence extends VulkanObject {
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
		final VulkanLibrary vulkan = device.vulkan();
		final NativeReference<Handle> ref = new Pointer(); // TODO
		vulkan.vkCreateFence(device, info, null, ref);

		// Create domain object
		return new Fence(ref.get(), device);
	}

	Fence(Handle handle, LogicalDevice device) {
		super(handle, device);
	}

	/**
	 * @return Whether this fence has been signalled
	 */
	public boolean signalled() {
		final LogicalDevice dev = this.device();
		final VkResult result = dev.vulkan().vkGetFenceStatus(dev, this);
		return switch(result) {
			case SUCCESS -> true;
			case NOT_READY -> false;
			default -> throw new VulkanException(result);
		};
	}

	/**
	 * Resets this fence.
	 * @see #reset(LogicalDevice, Collection)
	 */
	public void reset() {
		Fence.reset(device(), Set.of(this));
	}

	/**
	 * Blocks until this fence is ready.
	 * @see #wait(LogicalDevice, Collection, boolean, long)
	 */
	public void waitReady() {
		Fence.wait(device(), Set.of(this), true, Long.MAX_VALUE);
	}

	@Override
	protected Destructor<Fence> destructor(VulkanLibrary lib) {
		return lib::vkDestroyFence;
	}

	/**
	 * Resets a group of fences.
	 * @param device		Logical device
	 * @param fences		Fences to reset
	 */
	static void reset(LogicalDevice device, Collection<Fence> fences) {
		final Fence[] array = fences.toArray(Fence[]::new);
		device.vulkan().vkResetFences(device, array.length, array);
	}

	/**
	 * Waits for a group of fences.
	 * @param device		Logical device
	 * @param fences		Fences
	 * @param all			Whether to wait for all or any fence
	 * @param timeout		Timeout (nanoseconds)
	 */
	static void wait(LogicalDevice device, Collection<Fence> fences, boolean all, long timeout) {
		final Fence[] array = fences.toArray(Fence[]::new);
		device.vulkan().vkWaitForFences(device, array.length, array, all, timeout);
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
		int vkCreateFence(LogicalDevice device, VkFenceCreateInfo pCreateInfo, Handle pAllocator, NativeReference<Handle> pFence);

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
		int vkResetFences(LogicalDevice device, int fenceCount, Fence[] pFences);

		/**
		 * Retrieves the status of a given fence.
		 * Note that this method returns a {@link VkResult} status code.
		 * @param device
		 * @param fence
		 * @return Fence status flag
		 */
		VkResult vkGetFenceStatus(LogicalDevice device, Fence fence);

		/**
		 * Waits for a number of fences.
		 * @param device			Device
		 * @param fenceCount		Number of fences
		 * @param pFences			Fences
		 * @param waitAll			Whether to wait for <b>all</b> or <b>any</b> fence
		 * @param timeout			Timeout or {@link Long#MAX_VALUE} (nanoseconds)
		 * @return Result
		 */
		int vkWaitForFences(LogicalDevice device, int fenceCount, Fence[] pFences, boolean waitAll, long timeout);
	}
}
