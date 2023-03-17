package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>fence</i> is used to synchronise between application code and Vulkan.
 * @author Sarge
 */
public interface Fence extends NativeObject, TransientObject {
	/**
	 * @return Whether this fence has been signalled
	 */
	boolean signalled();

	/**
	 * Resets this fence.
	 * @see #reset(LogicalDevice, Collection)
	 */
	void reset();

	/**
	 * Resets a group of fences.
	 * @param dev			Logical device
	 * @param fences		Fences to reset
	 */
	static void reset(DeviceContext dev, Collection<Fence> fences) {
		final Pointer array = NativeObject.array(fences);
		final VulkanLibrary lib = dev.library();
		check(lib.vkResetFences(dev, fences.size(), array));
	}

	/**
	 * Waits for this fence.
	 * @see #wait(LogicalDevice, Collection, boolean, long)
	 */
	void waitReady();

	/**
	 * Waits for a group of fences.
	 * @param dev			Logical device
	 * @param fences		Fences
	 * @param all			Whether to wait for all or any fence
	 * @param timeout		Timeout (nanoseconds)
	 */
	static void wait(DeviceContext dev, Collection<Fence> fences, boolean all, long timeout) {
		final Pointer array = NativeObject.array(fences);
		final VulkanLibrary lib = dev.library();
		check(lib.vkWaitForFences(dev, fences.size(), array, all, timeout));
	}

	/**
	 * Creates a fence.
	 * @param dev			Logical device
	 * @param flags			Creation flags
	 * @return Fence
	 */
	static Fence create(DeviceContext dev, VkFenceCreateFlag... flags) {
		// Init descriptor
		final var info = new VkFenceCreateInfo();
		info.flags = BitMask.of(flags);

		// Create fence
		final VulkanLibrary lib = dev.library();
		final PointerByReference ref = dev.factory().pointer();
		check(lib.vkCreateFence(dev, info, null, ref));

		// Create domain object
		return new DefaultFence(new Handle(ref), dev);
	}

	/**
	 * Default implementation.
	 */
	final class DefaultFence extends VulkanObject implements Fence {
    	/**
    	 * Constructor.
    	 * @param handle		Handle
    	 * @param dev			Logical device
    	 */
    	DefaultFence(Handle handle, DeviceContext dev) {
    		super(handle, dev);
    	}

    	/**
    	 * @return Whether this fence has been signalled
    	 */
    	@Override
		public boolean signalled() {
    		final DeviceContext dev = this.device();
    		final VulkanLibrary lib = dev.library();
    		final VkResult result = lib.vkGetFenceStatus(dev, this);
    		return switch(result) {
    			case SUCCESS -> true;
    			case NOT_READY -> false;
    			default -> throw new VulkanException(result);
    		};
    	}

    	/**
    	 * Resets this fence.
    	 * @see Fence#reset(LogicalDevice, Collection)
    	 */
    	@Override
		public void reset() {
    		Fence.reset(device(), Set.of(this));
    	}

    	/**
    	 * Waits for this fence.
    	 * @see Fence#wait(LogicalDevice, Collection, boolean, long)
    	 */
    	@Override
		public void waitReady() {
    		Fence.wait(device(), Set.of(this), true, Long.MAX_VALUE);
    	}

    	@Override
    	protected Destructor<DefaultFence> destructor(VulkanLibrary lib) {
    		return lib::vkDestroyFence;
    	}
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
		int vkCreateFence(DeviceContext device, VkFenceCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pFence);

		/**
		 * Destroys a fence.
		 * @param device			Device
		 * @param fence				Fence
		 * @param pAllocator		Allocator
		 */
		void vkDestroyFence(DeviceContext device, Fence fence, Pointer pAllocator);

		/**
		 * Resets a number of fences.
		 * @param device			Device
		 * @param fenceCount		Number of fences
		 * @param pFences			Fences
		 * @return Result
		 */
		int vkResetFences(DeviceContext device, int fenceCount, Pointer pFences);

		/**
		 * Retrieves the status of a given fence.
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
		int vkWaitForFences(DeviceContext device, int fenceCount, Pointer pFences, boolean waitAll, long timeout);
	}
}
