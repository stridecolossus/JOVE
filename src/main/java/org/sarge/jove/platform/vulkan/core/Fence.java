package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;

import java.util.Collection;
import java.util.Set;

import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkFenceCreateFlag;
import org.sarge.jove.platform.vulkan.VkFenceCreateInfo;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.IntegerEnumeration;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>fence</i> is used to synchronise between the host and a queue.
 * @author Sarge
 */
public class Fence extends AbstractVulkanObject {
	private static final int SIGNALLED = VkResult.SUCCESS.value();
	private static final int NOT_SIGNALLED = VkResult.NOT_READY.value();

	/**
	 * Creates a fence.
	 * @param dev			Logical device
	 * @param flags			Fence flags
	 * @return New fence
	 * @throws VulkanException if the fence cannot be created
	 */
	public static Fence create(DeviceContext dev, VkFenceCreateFlag... flags) {
		// Init descriptor
		final VkFenceCreateInfo info = new VkFenceCreateInfo();
		info.flags = IntegerEnumeration.mask(flags);

		// Create fence
		final VulkanLibrary lib = dev.library();
		final PointerByReference handle = lib.factory().pointer();
		check(lib.vkCreateFence(dev, info, null, handle));

		// Create domain object
		return new Fence(handle.getValue(), dev);
	}

	/**
	 * Resets a group of fences.
	 * @param dev			Logical device
	 * @param fences		Fences to reset
	 * @throws VulkanException if the fences cannot be reset
	 */
	public static void reset(DeviceContext dev, Collection<Fence> fences) {
		final Pointer array = NativeObject.toArray(fences);
		final VulkanLibrary lib = dev.library();
		check(lib.vkResetFences(dev, fences.size(), array));
	}

	/**
	 * Waits for a group of fences.
	 * @param dev			Logical device
	 * @param fences		Fences
	 * @param all			Whether to wait for all or any fence
	 * @param timeout		Timeout (ms)
	 * @throws VulkanException if the API method fails
	 */
	public static void wait(DeviceContext dev, Collection<Fence> fences, boolean all, long timeout) {
		final Pointer array = NativeObject.toArray(fences);
		final VulkanLibrary lib = dev.library();
		check(lib.vkWaitForFences(dev, fences.size(), array, VulkanBoolean.of(all), timeout));
	}

	/**
	 * Constructor.
	 * @param handle		Handle
	 * @param dev			Logical device
	 */
	Fence(Pointer handle, DeviceContext dev) {
		super(handle, dev);
	}

	/**
	 * @return Whether this fence has been signalled
	 * @throws VulkanException if the status cannot be retrieved
	 */
	public boolean signalled() {
		final DeviceContext dev = this.device();
		final VulkanLibrary lib = dev.library();
		final int result = lib.vkGetFenceStatus(dev, this);
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

	@Override
	protected Destructor<Fence> destructor(VulkanLibrary lib) {
		return lib::vkDestroyFence;
	}
}
