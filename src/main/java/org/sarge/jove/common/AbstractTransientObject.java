package org.sarge.jove.common;

/**
 * Partial implementation that tracks whether this object has been released.
 * @author Sarge
 */
public abstract class AbstractTransientObject implements TransientObject {
	private boolean destroyed;

	@Override
	public final boolean isDestroyed() {
		return destroyed;
	}

	@Override
	public void destroy() {
		if(destroyed) {
			throw new IllegalStateException("Transient object has already been destroyed: " + this);
		}
		release();
		destroyed = true;
	}

	/**
	 * Releases resources managed by this object.
	 * @see #destroy()
	 */
	protected void release() {
		// Does nowt
	}
}
