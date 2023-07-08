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
		 * @param device		Logical device
		 * @param object		Native object to destroy
		 * @param allocator		Vulkan memory allocator (always {@code null})
		 */
		void destroy(DeviceContext device, T object, Pointer allocator);
	}

	private final DeviceContext device;

	/**
	 * Constructor.
	 * @param handle		Object handle
	 * @param device		Logical device
	 */
	protected VulkanObject(Handle handle, DeviceContext device) {
		super(handle);
		this.device = notNull(device);
	}

	/**
	 * @return Logical device
	 */
	public final DeviceContext device() {
		return device;
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
		final Destructor destructor = destructor(device.library());
		destructor.destroy(device, this, null);
		super.destroy();
	}

	@Override
	protected void release() {
		// Does nowt
	}

	@Override
	public int hashCode() {
		return handle.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}
}
