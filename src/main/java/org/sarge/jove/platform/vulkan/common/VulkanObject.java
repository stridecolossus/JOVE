package org.sarge.jove.platform.vulkan.common;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;

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
		void destroy(DeviceContext device, T object, Handle allocator);
	}

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
	 * Provides the <i>destructor</i> API method for this object.
	 * @param lib Vulkan API
	 * @return Destructor method
	 */
	protected abstract Destructor<?> destructor(VulkanLibrary lib);

	@SuppressWarnings("unchecked")
	@Override
	public final void destroy() {
//		try {
//			final Method method = VulkanLibrary.class.getDeclaredMethod(destructor(), DeviceContext.class, this.getClass(), Handle.class);
//			method.invoke(device, this, null);
//		}
//		catch(Exception e) {
//			throw new IllegalArgumentException();
//		}

		@SuppressWarnings("rawtypes")
		final Destructor destructor = destructor(device.vulkan().library());
		destructor.destroy(device, this, null);

		super.destroy(); // TODO - should be done before?
	}

//	protected abstract String destructor();

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
