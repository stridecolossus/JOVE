package org.sarge.jove.common;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.sun.jna.Pointer;

/**
 * A <i>transient native object</i> is a template implementation for a native object that is managed by the application.
 */
public abstract class AbstractTransientNativeObject implements TransientNativeObject {
	protected final Handle handle;

	private boolean destroyed;

	/**
	 * Constructor.
	 * @param handle Native handle
	 */
	protected AbstractTransientNativeObject(Pointer handle) {
		this.handle = new Handle(handle);
	}

	/**
	 * Constructor.
	 * @param handle Handle
	 */
	protected AbstractTransientNativeObject(Handle handle) {
		this.handle = notNull(handle);
	}

	@Override
	public Handle handle() {
		return handle;
	}

	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

	@Override
	public synchronized void destroy() {
		if(destroyed) throw new IllegalStateException("Object has already been destroyed: " + this);
		release();
		destroyed = true;
	}

	/**
	 * Releases this object.
	 * @see #destroy()
	 */
	protected abstract void release();

	/**
	 * Restores this object.
	 * @throws IllegalStateException if this object has not been destroyed
	 */
	protected synchronized void restore() {
		if(!destroyed) throw new IllegalStateException("Object has not been destroyed: " + this);
		destroyed = false;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("handle", handle).build();
	}
}
