package org.sarge.jove.platform.vulkan.common;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;

import com.sun.jna.Pointer;

/**
 * An <i>abstract Vulkan object</i> is the template base-class for an object derived from the logical device.
 * @author Sarge
 */
public abstract class AbstractVulkanObject extends AbstractTransientNativeObject {
	/**
	 * A <i>destructor</i> abstracts the API method used to destroy this object.
	 * @see AbstractVulkanObject#destructor(VulkanLibrary)
	 */
	@FunctionalInterface
	public interface Destructor<T extends AbstractVulkanObject> {
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
	 * @param dev			Device context
	 */
	protected AbstractVulkanObject(Pointer handle, DeviceContext dev) {
		this(new Handle(handle), dev);
	}

	/**
	 * Constructor.
	 * @param handle		Object handle
	 * @param dev			Device context
	 */
	protected AbstractVulkanObject(Handle handle, DeviceContext dev) {
		super(handle);
		this.dev = notNull(dev);
	}

	/**
	 * @return Logical device
	 */
	public DeviceContext device() {
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
	public synchronized void destroy() {
		// Destroy this object
		@SuppressWarnings("rawtypes")
		final Destructor destructor = destructor(dev.library());
		destructor.destroy(dev, this, null);

		// Delegate
		super.destroy();
	}

	@Override
	protected void release() {
		// Does nowt
	}
}
