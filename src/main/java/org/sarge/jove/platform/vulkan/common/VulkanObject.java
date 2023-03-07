package org.sarge.jove.platform.vulkan.common;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;

import com.sun.jna.Pointer;

/**
 * A <i>Vulkan object</i> is a template base-class for objects derived from the logical device.
 * @author Sarge
 */
public abstract class VulkanObject extends TransientNativeObject {
	/**
	 * A <i>destructor</i> abstracts the API method used to destroy this object.
	 * @see VulkanObject#destructor(VulkanLibrary)
	 */
	@FunctionalInterface
	public interface Destructor<T extends VulkanObject> {
		/**
		 * Destroys this object.
		 * @param dev			Logical device
		 * @param obj			Native object to destroy
		 * @param allocator		Vulkan memory allocator (always {@code null})
		 */
		void destroy(DeviceContext dev, T obj, Pointer allocator);
	}

	private final DeviceContext dev;

	/**
	 * Constructor.
	 * @param handle		Object handle
	 * @param dev			Logical device
	 */
	protected VulkanObject(Handle handle, DeviceContext dev) {
		super(handle);
		this.dev = notNull(dev);
	}

	/**
	 * @return Logical device
	 */
	public final DeviceContext device() {
		return dev;
	}

	/**
	 * Provides the <i>destructor</i> API method for this object.
	 * @param lib Vulkan API
	 * @return Destructor method
	 */
	protected abstract Destructor<?> destructor(VulkanLibrary lib);

	// TODO - all refer to compound VulkanLibrary, could it be parameterized?

	@Override
	@SuppressWarnings("unchecked")
	public final void destroy() {
		@SuppressWarnings("rawtypes")
		final Destructor destructor = destructor(dev.library());
		destructor.destroy(dev, this, null);
		super.destroy();
	}

	@Override
	protected void release() {
		// Does nowt
	}
}
