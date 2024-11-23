package org.sarge.jove.platform.vulkan.core;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.PointerReference;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.BitMask;

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
	public static Fence create(DeviceContext device, VkFenceCreateFlag... flags) {
		// Init descriptor
		final var info = new VkFenceCreateInfo();
		info.flags = BitMask.of(flags);

		// Create fence
		final Vulkan vulkan = device.vulkan();
		final PointerReference ref = vulkan.factory().pointer();
		vulkan.library().vkCreateFence(device, info, null, ref);

		// Create domain object
		return new Fence(ref.handle(), device);
	}

	Fence(Handle handle, DeviceContext device) {
		super(handle, device);
	}

	/**
	 * @return Whether this fence has been signalled
	 */
	public boolean signalled() {
		final DeviceContext dev = this.device();
		final VkResult result = dev.vulkan().library().vkGetFenceStatus(dev, this);
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
	static void reset(DeviceContext device, Collection<Fence> fences) {
		device.vulkan().library().vkResetFences(device, fences.size(), fences);
	}

	/**
	 * Waits for a group of fences.
	 * @param device		Logical device
	 * @param fences		Fences
	 * @param all			Whether to wait for all or any fence
	 * @param timeout		Timeout (nanoseconds)
	 */
	static void wait(DeviceContext device, Collection<Fence> fences, boolean all, long timeout) {
		device.vulkan().library().vkWaitForFences(device, fences.size(), fences, all, timeout);
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
		int vkCreateFence(DeviceContext device, VkFenceCreateInfo pCreateInfo, Handle pAllocator, PointerReference pFence);

		/**
		 * Destroys a fence.
		 * @param device			Device
		 * @param fence				Fence
		 * @param pAllocator		Allocator
		 */
		void vkDestroyFence(DeviceContext device, Fence fence, Handle pAllocator);

		/**
		 * Resets a number of fences.
		 * @param device			Device
		 * @param fenceCount		Number of fences
		 * @param pFences			Fences
		 * @return Result
		 */
		int vkResetFences(DeviceContext device, int fenceCount, Collection<Fence> pFences);

		/**
		 * Retrieves the status of a given fence.
		 * Note that this method returns a {@link VkResult} status code.
		 * @param device
		 * @param fence
		 * @return Fence status flag
		 */
		VkResult vkGetFenceStatus(DeviceContext device, Fence fence);

		/**
		 * Waits for a number of fences.
		 * @param device			Device
		 * @param fenceCount		Number of fences
		 * @param pFences			Fences
		 * @param waitAll			Whether to wait for <b>all</b> or <b>any</b> fence
		 * @param timeout			Timeout or {@link Long#MAX_VALUE} (nanoseconds)
		 * @return Result
		 */
		int vkWaitForFences(DeviceContext device, int fenceCount, Collection<Fence> pFences, boolean waitAll, long timeout);
	}
}
