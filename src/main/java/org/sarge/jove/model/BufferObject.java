package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Base-class for vertex and index buffers.
 * @author Sarge
 */
public abstract class BufferObject {
	/**
	 * Update modes.
	 */
	public enum Mode {
		/**
		 * Fixed buffer that is pushed once.
		 */
		STATIC,

		/**
		 * Dynamic buffer that can be pushed programmatically.
		 */
		DYNAMIC,

		/**
		 * Buffer that is pushed every frame.
		 */
		STREAM
	}

	private final Mode mode;

	/**
	 * Constructor.
	 * @param mode Update mode
	 */
	protected BufferObject(Mode mode) {
		this.mode = notNull(mode);
	}

	/**
	 * @return Component size of this buffer
	 */
	public abstract int size();

	/**
	 * @return Length of this buffer
	 */
	public abstract int length();

	/**
	 * @return Update mode
	 */
	public final Mode mode() {
		return mode;
	}

	/**
	 * @throws IllegalStateException if this buffer is not mutable
	 */
	protected void checkMutable() {
		if(mode == Mode.STATIC) throw new IllegalStateException("Buffer cannot be modified");
	}

	/**
	 * Pushes this buffer to the graphics system.
	 */
	public abstract void push();

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
