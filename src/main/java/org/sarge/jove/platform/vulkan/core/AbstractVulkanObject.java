package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.util.Check.notNull;

import org.sarge.jove.common.NativeObject;
import org.sarge.jove.common.NativeObject.TransientNativeObject;

import com.sun.jna.Pointer;

/**
 * Convenience base-class for a Vulkan object derived from the logical device.
 * @author Sarge
 */
public abstract class AbstractVulkanObject implements TransientNativeObject {
	/**
	 * Destructor method.
	 */
	@FunctionalInterface
	public interface Destructor {
		/**
		 * Destroys this object.
		 * @param dev			Logical device
		 * @param handle		Handle
		 * @param allocator		Allocator
		 */
		void destroy(Handle dev, Handle handle, Handle allocator);
	}

	/**
	 * Helper - Extracts the handle from the given optional object.
	 * @param obj Native object
	 * @return Handle of the given object or {@code null}
	 */
	protected static Handle handle(NativeObject obj) {
		if(obj == null) {
			return null;
		}
		else {
			return obj.handle();
		}
	}

	private final Handle handle;
	private final LogicalDevice dev;
	private final Destructor destructor;

	private boolean destroyed;

	/**
	 * Constructor.
	 * @param handle		JNA pointer for this handle
	 * @param dev			Parent logical device
	 * @param destructor	Destructor API method
	 */
	protected AbstractVulkanObject(Pointer handle, LogicalDevice dev, Destructor destructor) {
		this(new Handle(handle), dev, destructor);
	}

	/**
	 * Constructor.
	 * @param handle		Handle
	 * @param dev			Parent logical device
	 * @param destructor	Destructor API method
	 */
	protected AbstractVulkanObject(Handle handle, LogicalDevice dev, Destructor destructor) {
		this.handle = notNull(handle);
		this.dev = notNull(dev);
		this.destructor = notNull(destructor);
	}

	@Override
	public Handle handle() {
		return handle;
	}

	/**
	 * @return Parent logical device
	 */
	public LogicalDevice device() {
		return dev;
	}

	/**
	 * @return Whether this object has been destroyed
	 */
	public boolean isDestroyed() {
		return destroyed;
	}

	/**
	 * Destroys this object.
	 * @throws IllegalStateException if this object has already been destroyed
	 */
	@Override
	public synchronized void destroy() {
		if(destroyed) throw new IllegalStateException("Object has already been destroyed: " + this);
		destructor.destroy(dev.handle(), handle, null);
		destroyed = true;
	}
}
