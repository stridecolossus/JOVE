package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;

import com.sun.jna.Pointer;

/**
 * Convenience base-class for a Vulkan object derived from the logical device.
 * @author Sarge
 */
public abstract class AbstractVulkanObject {
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

	private Handle handle;
	private final LogicalDevice dev;
	private final Destructor destructor;

	/**
	 * Constructor.
	 * @param handle		Pipeline handle
	 * @param dev			Parent logical device
	 * @param destructor	Destructor API method
	 */
	protected AbstractVulkanObject(Pointer handle, LogicalDevice dev, Destructor destructor) {
		this(new Handle(handle), dev, destructor);
//		this.handle = new Handle(handle);
//		this.dev = notNull(dev);
//		this.destructor = notNull(destructor);
	}

	protected AbstractVulkanObject(Handle handle, LogicalDevice dev, Destructor destructor) {
		this.handle = notNull(handle);
		this.dev = notNull(dev);
		this.destructor = notNull(destructor);
	}

	/**
	 * @return Handle
	 */
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
		return handle == null;
	}

	/**
	 * Destroys this object.
	 * @throws IllegalStateException if this object has already been destroyed
	 */
	public synchronized void destroy() {
		if(isDestroyed()) throw new IllegalStateException("Object has already been destroyed: " + this);
		destructor.destroy(dev.handle(), handle, null);
		handle = null;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
