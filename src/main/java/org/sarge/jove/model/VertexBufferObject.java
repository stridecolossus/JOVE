package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.BufferFactory;
import org.sarge.lib.collection.StrictList;

/**
 * A <i>vertex buffer object</i> (VBO) contains floating-point vertex data to be pushed to the graphics system.
 * @author Sarge
 */
public class VertexBufferObject extends BufferObject {
	/**
	 * VBO attribute descriptor.
	 */
	public static final class Attribute {
		private final int size;
//		private final int location;
//		private final int offset;

		// TODO - size of each (bytes) => convert float -> n bytes, i.e. could have smaller than float
		// - map to VkFormat
		// - do all the components HAVE to be the same size?

		/**
		 * Constructor.
		 * @param size Component size of this buffer attribute
		 */
		public Attribute(int size) {
			this.size = oneOrMore(size);
		}

		/**
		 * @return Component size of this attribute
		 */
		public int size() {
			return size;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	private final List<Attribute> layout;
	private final FloatBuffer buffer;
	private final int size; // TODO - stride
	// private final int binding?

	/**
	 * Constructor.
	 * @param layout		Buffer layout
	 * @param mode			Update mode
	 * @param buffer		Buffer
	 */
	public VertexBufferObject(List<Attribute> layout, Mode mode, FloatBuffer buffer) {
		super(mode);
		this.layout = List.copyOf(notEmpty(layout));
		this.buffer = notNull(buffer);
		this.size = size(layout);
	}

	/**
	 * @return Buffer layout
	 */
	public List<Attribute> layout() {
		return layout;
	}

	/**
	 * Calculates the total component size of the given buffer layout.
	 */
	private static int size(List<Attribute> layout) {
		return layout.stream().mapToInt(Attribute::size).sum();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public int length() {
		return buffer.capacity();
	}

	/**
	 * Updates this buffer.
	 * @param data Data to buffer
	 */
	public void update(Stream<Bufferable> data) {
		checkMutable();
		data.forEach(b -> b.buffer(buffer));
		buffer.flip();
	}

	@Override
	public void push() {
		// TODO
	}

	/**
	 * Builder for a vertex buffer.
	 */
	public static class Builder {
		private final List<Attribute> layout = new StrictList<>();
		private Mode mode = Mode.STATIC;
		private int len;

		/**
		 * Adds an attribute to this buffer.
		 * @param attr Attribute
		 */
		public Builder attribute(Attribute attr) {
			layout.add(attr);
			return this;
		}

		/**
		 * Sets the update mode of this buffer.
		 * @param mode Update mode
		 */
		public Builder mode(Mode mode) {
			this.mode = notNull(mode);
			return this;
		}

		/**
		 * Sets the length of the buffer.
		 * @param len Buffer length
		 */
		public Builder length(int len) {
			this.len = zeroOrMore(len);
			return this;
		}

		/**
		 * Constructs this buffer.
		 * @return New buffer
		 */
		public VertexBufferObject build() {
			final FloatBuffer buffer = BufferFactory.floatBuffer(len * size(layout));
			return new VertexBufferObject(layout, mode, buffer);
		}
	}
}
