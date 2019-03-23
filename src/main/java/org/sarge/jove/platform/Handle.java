package org.sarge.jove.platform;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractEqualsObject;

import com.sun.jna.Pointer;

/**
 * A <i>handle</i> is a wrapper for a {@link Pointer} to a native resource.
 * @author Sarge
 */
public abstract class Handle extends AbstractEqualsObject implements Resource {
	private Pointer handle;

	/**
	 * Constructor.
	 * @param handle Resource handle
	 */
	protected Handle(Pointer handle) {
		this.handle = notNull(handle);
	}

	/**
	 * @return Whether this resource has been destroyed
	 */
	public boolean isDestroyed() {
		return handle == null;
	}

	/**
	 * @return Handle
	 * @throws IllegalStateException if this handle has been destroyed
	 */
	public Pointer handle() {
		if(handle == null) throw new IllegalStateException("Handle has been destroyed: " + this);
		return handle;
	}

	@Override
	public synchronized void destroy() {
		if(handle == null) throw new IllegalStateException("Handle has already been destroyed: " + this);
		handle = null;
	}
}
