package org.sarge.jove.platform.vulkan.common;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;

/**
 * A <i>Vulkan object</i> is a template base-class for objects derived from the logical device.
 * @author Sarge
 */
public abstract class VulkanObject extends TransientNativeObject {
	private final DeviceContext device;

	/**
	 * Constructor.
	 * @param handle		Object handle
	 * @param device		Logical device
	 */
	protected VulkanObject(Handle handle, DeviceContext device) {
		super(handle);
		this.device = requireNonNull(device);
	}

	/**
	 * @return Logical device
	 */
	public final DeviceContext device() {
		return device;
	}

	/**
	 * A <i>destructor</i> abstracts the API method used to destroy this object.
	 * @see VulkanObject#destructor(VulkanLibrary)
	 */
	@FunctionalInterface
	protected interface Destructor<T extends VulkanObject> {
		/**
		 * Destroys this object.
		 * @param device		Logical device
		 * @param object		Native object to destroy
		 * @param allocator		Vulkan memory allocator (always {@code null})
		 */
		void destroy(DeviceContext device, T object, Handle allocator);
	}

	/**
	 * Provides the <i>destructor</i> API method for this object.
	 * @param lib Vulkan API
	 * @return Destructor method
	 */
	protected abstract Destructor<?> destructor(VulkanLibrary lib);

	@SuppressWarnings("unchecked")
	@Override
	public final void destroy() {
		@SuppressWarnings("rawtypes")
		final Destructor destructor = destructor(device.vulkan().library());
		destructor.destroy(device, this, null);
		super.destroy(); // TODO - should be done before?
	}

	@Override
	protected void release() {
		// Does nowt
	}

	@Override
	public int hashCode() {
		return handle.hashCode();
	}

	// TODO - bad
	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}
}
