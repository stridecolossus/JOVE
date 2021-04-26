package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;

import java.util.Collection;
import java.util.Set;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle.HandleArray;
import org.sarge.jove.platform.vulkan.VkFenceCreateFlag;
import org.sarge.jove.platform.vulkan.VkFenceCreateInfo;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.platform.vulkan.util.VulkanException;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>fence</i> is used to synchronise between the host and a queue.
 * @author Sarge
 */
public class Fence extends AbstractVulkanObject {
	private static final int SIGNALLED = VkResult.VK_SUCCESS.value();
	private static final int NOT_SIGNALLED = VkResult.VK_NOT_READY.value();

	/**
	 * Creates a fence.
	 * @param dev			Logical device
	 * @param flags			Fence flags
	 * @return New fence
	 * @throws VulkanException if the fence cannot be created
	 */
	public static Fence create(LogicalDevice dev, VkFenceCreateFlag... flags) {
		// Init descriptor
		final VkFenceCreateInfo info = new VkFenceCreateInfo();
		info.flags = IntegerEnumeration.mask(flags);

		// Create fence
		final VulkanLibrary lib = dev.library();
		final PointerByReference handle = lib.factory().pointer();
		check(lib.vkCreateFence(dev.handle(), info, null, handle));

		// Create domain object
		return new Fence(handle.getValue(), dev);
	}

	/**
	 * Resets a group of fences.
	 * @param dev			Logical device
	 * @param fences		Fences to reset
	 * @throws VulkanException if the fences cannot be reset
	 */
	public static void reset(LogicalDevice dev, Collection<Fence> fences) {
		final var array = Handle.toArray(fences);
		final VulkanLibrary lib = dev.library();
		check(lib.vkResetFences(dev.handle(), fences.size(), array));
	}

	/**
	 * Waits for a group of fences.
	 * @param dev			Logical device
	 * @param fences		Fences
	 * @param all			Whether to wait for all or any fence
	 * @param timeout		Timeout (ms)
	 * @throws VulkanException if the API method fails
	 */
	public static void wait(LogicalDevice dev, Collection<Fence> fences, boolean all, long timeout) {
		final HandleArray array = Handle.toArray(fences);
		final VulkanLibrary lib = dev.library();
		check(lib.vkWaitForFences(dev.handle(), fences.size(), array, VulkanBoolean.of(all), timeout));
	}

	/**
	 * Constructor.
	 * @param handle		Handle
	 * @param dev			Logical device
	 */
	Fence(Pointer handle, LogicalDevice dev) {
		super(handle, dev, dev.library()::vkDestroyFence);
	}

	/**
	 * @return Whether this fence has been signalled
	 * @throws VulkanException if the status cannot be retrieved
	 */
	public boolean signalled() {
		final LogicalDevice dev = this.device();
		final VulkanLibrary lib = dev.library();
		final int result = lib.vkGetFenceStatus(dev.handle(), this.handle());
		if(result == SIGNALLED) {
			return true;
		}
		else
		if(result == NOT_SIGNALLED) {
			return false;
		}
		else {
			throw new VulkanException(result);
		}
		// Note - cannot use switch expression here (values must be constants)
	}

	/**
	 * Resets this fence.
	 * @see #reset(LogicalDevice, Collection)
	 */
	public void reset() {
		reset(device(), Set.of(this));
	}

	/**
	 * Waits for this fence.
	 * @see #wait(LogicalDevice, Collection, boolean, long)
	 */
	public void waitReady() {
		wait(device(), Set.of(this), true, Long.MAX_VALUE);
	}
}
