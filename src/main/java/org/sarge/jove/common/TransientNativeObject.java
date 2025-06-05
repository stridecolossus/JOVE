package org.sarge.jove.common;

import static java.util.Objects.requireNonNull;

/**
 * Template implementation for a native object managed by the application.
 * @author Sarge
 */
public abstract class TransientNativeObject implements NativeObject, TransientObject {
	protected final Handle handle;

	private boolean destroyed;

	/**
	 * Constructor.
	 * @param handle Handle
	 */
	protected TransientNativeObject(Handle handle) {
		this.handle = requireNonNull(handle);
	}

	@Override
	public final Handle handle() {
		return handle;
	}

	@Override
	public final boolean isDestroyed() {
		return destroyed;
	}

	@Override
	public void destroy() {
		if(destroyed) throw new IllegalStateException("Transient object has already been destroyed: " + this);
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
				(obj instanceof TransientNativeObject that) &&
				this.handle.equals(that.handle());
	}

	@Override
	public String toString() {
		return handle.toString();
	}
}
