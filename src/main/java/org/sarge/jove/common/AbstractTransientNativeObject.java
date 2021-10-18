package org.sarge.jove.common;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Template implementation for a native object managed by the application.
 * @author Sarge
 */
public abstract class AbstractTransientNativeObject implements TransientNativeObject {
	protected final Handle handle;

	private boolean destroyed;

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
	public final boolean isDestroyed() {
		return destroyed;
	}

	// TODO - final
	@Override
	public synchronized void close() {
		if(destroyed) throw new IllegalStateException("Object has already been destroyed: " + this);
		release();
		destroyed = true;
	}

	/**
	 * Releases resources managed by this object.
	 * @see #close()
	 */
	protected abstract void release();

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(handle).build();
	}
}
