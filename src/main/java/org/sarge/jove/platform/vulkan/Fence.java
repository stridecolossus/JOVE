package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Collection;
import java.util.function.IntSupplier;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>fence</i> is a Vulkan synchronisation mechanism.
 * @author Sarge
 */
public class Fence extends LogicalDeviceHandle {
	/**
	 * Creates a fence.
	 * @param dev Logical device
	 * @return Fence
	 */
	public static Fence create(LogicalDevice dev, boolean init) {
		// Init descriptor
		final VkFenceCreateInfo info = new VkFenceCreateInfo();
		if(init) {
			info.flags |= VkFenceCreateFlag.VK_FENCE_CREATE_SIGNALED_BIT.value();
		}

		// Allocate fence
		final Vulkan vulkan = dev.api();
		final VulkanLibrarySynchronize lib = vulkan.api();
		final PointerByReference fence = vulkan.factory().reference();
		check(lib.vkCreateFence(dev.handle(), info, null, fence));

		// Create fence
		final Pointer handle = fence.getValue();
		final IntSupplier status = () -> lib.vkGetFenceStatus(dev.handle(), handle);
		return new Fence(fence.getValue(), dev, status);
	}

	/**
	 * Group of fences.
	 */
	public static class Group extends AbstractEqualsObject {
		private final LogicalDevice dev;
		private final Pointer[] fences;

		/**
		 * Constructor.
		 * @param dev		Device
		 * @param fences	Fences
		 * @throws IllegalArgumentException if the fences is empty
		 */
		public Group(LogicalDevice dev, Collection<Fence> fences) {
			Check.notEmpty(fences);
			this.dev = notNull(dev);
			this.fences = fences.stream().map(Handle::handle).toArray(Pointer[]::new);
		}

		/**
		 * Waits for the fences in this group.
		 * @param all Whether to wait for <b>all</b> fences or <b>any</b> fence in this group
		 */
		public void wait(boolean all) {
			final VulkanLibrarySynchronize lib = dev.api().api();
			check(lib.vkWaitForFences(dev.handle(), fences.length, fences, VulkanBoolean.of(all), Long.MAX_VALUE));
		}

		/**
		 * Resets <b>all</b> the fences in this group.
		 */
		public void reset() {
			final VulkanLibrarySynchronize lib = dev.api().api();
			check(lib.vkResetFences(dev.handle(), fences.length, fences));
		}
	}

	/**
	 * Fence status.
	 * @see VkResult
	 */
	public enum Status {
		SIGNALED,
		NOT_READY,
		LOST,
	}

	private final IntSupplier status;

	/**
	 * Constructor.
	 * @param handle 		Handle
	 * @param dev			Logical device
	 * @param status		Status supplier
	 */
	Fence(Pointer handle, LogicalDevice dev, IntSupplier status) {
		super(handle, dev, lib -> lib::vkDestroyFence);
		this.status = notNull(status);
	}

	/**
	 * @return Fence status
	 * @throws RuntimeException if the returned Vulkan status is not recognised
	 */
	public Status status() {
		final VkResult result = IntegerEnumeration.map(VkResult.class, status.getAsInt());
		switch(result) {
		case VK_SUCCESS:				return Status.SIGNALED;
		case VK_NOT_READY:				return Status.NOT_READY;
		case VK_ERROR_DEVICE_LOST:		return Status.LOST;
		default:						throw new RuntimeException("Unexpected fence status: " + result);
		}
	}
}
