package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.IntStream;

import org.sarge.jove.common.*;
import org.sarge.jove.util.Mask;

/**
 * TODO
 * @author Sarge
 */
public class IndexedModel extends DefaultModel {
	/**
	 * Size of a {@code short} index.
	 */
	private static final long SHORT_INDEX = Mask.unsignedMaximum(Short.SIZE);

	/**
	 * Determines whether the given draw count requires an {@code int} index.
	 * @param count Draw count
	 * @return Whether the index data type is integral
	 */
	public static boolean isIntegerIndex(int count) {
		return count >= SHORT_INDEX;
	}

	private final List<Integer> index = new ArrayList<>();
	private int restart;
	private boolean compact = true;

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 */
	public IndexedModel(Primitive primitive, Layout layout) {
		super(primitive, layout);
	}

	@Override
	public int count() {
		return index.size() - restart;
	}

	@Override
	public boolean isIndexed() {
		return true;
	}

	/**
	 * @return Index
	 */
	public IntStream index() {
		return index.stream().mapToInt(Integer::intValue);
	}

	/**
	 * Adds a vertex index to this model.
	 * @param index Vertex index
	 * @throws IndexOutOfBoundsException if {@link #index} is not a valid vertex index
	 */
	public IndexedModel add(int index) {
		if((index < 0) || (index >= super.count())) throw new IndexOutOfBoundsException(index);
		this.index.add(index);
		return this;
	}

	/**
	 * Restarts the index.
	 */
	public IndexedModel restart() {
		index.add(-1);
		++restart;
		return this;
	}

	/**
	 * Sets whether the index buffer uses the most <i>compact</i> data type (default is {@code true}).
	 * <p>
	 * If {@link #compact} is set, the data type of the index buffer is {@code short} if the index is small enough.
	 * Otherwise the index is comprised of {@code int} values.
	 * TODO - revise doc, restart precludes, note still stored as integers
	 * <p>
	 * @param compact Whether to use compact indices
	 * @see #isIntegerIndex(int)
	 */
	public IndexedModel compact(boolean compact) {
		this.compact = compact;
		return this;
	}

	/**
	 * Mesh index buffer.
	 */
	private class IndexBuffer implements ByteSizedBufferable {
		/**
		 * @return Whether the index requires a {@code int} type
		 */
		private boolean isIntegral() {
			if(compact) {
				return (restart > 0) || isIntegerIndex(index.size());
			}
			else {
				return true;
			}
		}
		// TODO - move compact to parameter of mesh factory?

		@Override
		public int length() {
			final int bytes = isIntegral() ? Integer.BYTES : Short.BYTES;
			return index.size() * bytes;
		}

		@Override
		public void buffer(ByteBuffer bb) {
			if(isIntegral())  {
				if(bb.isDirect()) {
					for(int n : index) {
						bb.putInt(n);
					}
				}
				else {
					final int[] indices = index().toArray();
					bb.asIntBuffer().put(indices);
// TODO - does not update the position!!!
//					bb.position(bb.position() + indices.length * Integer.BYTES);
				}
			}
			else {
				for(int n : index) {
					bb.putShort((short) n);
				}
			}
		}
	}

	@Override
	public BufferedModel buffer() {
		return new BufferedModel(this, new VertexBuffer(), new IndexBuffer());
	}
}
