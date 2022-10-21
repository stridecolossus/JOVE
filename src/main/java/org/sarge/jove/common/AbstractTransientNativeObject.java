package org.sarge.jove.common;

import static org.sarge.lib.util.Check.notNull;

/**
 * Template implementation for a native object managed by the application.
 * @author Sarge
 */
public abstract class AbstractTransientNativeObject implements NativeObject, TransientObject {
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

	@Override
	public void destroy() {
		if(destroyed) throw new IllegalStateException("Object has already been destroyed: " + this);
		release();
		destroyed = true;
	}

	/**
	 * Releases resources managed by this object.
	 * @see #destroy()
	 */
	protected abstract void release();

	@Override
	public int hashCode() {
		return handle.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof AbstractTransientNativeObject that) &&
				(this.destroyed == that.isDestroyed()) &&
				this.handle.equals(that.handle());
	}

	@Override
	public String toString() {
		return handle.toString();
	}
}
