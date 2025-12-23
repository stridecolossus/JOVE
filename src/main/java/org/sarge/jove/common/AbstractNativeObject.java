package org.sarge.jove.common;

import static java.util.Objects.requireNonNull;

/**
 * Template implementation for a native object managed by the application.
 * @author Sarge
 */
public abstract class AbstractNativeObject extends AbstractTransientObject implements NativeObject {
	private final Handle handle;

	/**
	 * Constructor.
	 * @param handle Handle
	 */
	protected AbstractNativeObject(Handle handle) {
		this.handle = requireNonNull(handle);
	}

	@Override
	public final Handle handle() {
		return handle;
	}

	@Override
	public int hashCode() {
		return handle.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof AbstractNativeObject that) &&
				this.handle.equals(that.handle());
	}

	@Override
	public String toString() {
		final String name = this.getClass().getSimpleName();
		return String.format("%s[%s]", name, handle);
	}
}
