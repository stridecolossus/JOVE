package org.sarge.jove.lib;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.common.TransientObject;

/**
 * Template implementation for a native object managed by the application.
 * @author Sarge
 */
public abstract class TransientNativeObjectTEMP implements NativeObjectTEMP, TransientObject {
	protected final Handle handle;

	private boolean destroyed;

	/**
	 * Constructor.
	 * @param handle Handle
	 */
	protected TransientNativeObjectTEMP(Handle handle) {
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
				(obj instanceof TransientNativeObjectTEMP that) &&
				this.handle.equals(that.handle());
	}

	@Override
	public String toString() {
		return handle.toString();
	}
}
