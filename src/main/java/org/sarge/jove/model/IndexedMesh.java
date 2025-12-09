package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.Layout;
import org.sarge.jove.util.MathsUtility;

/**
 * An <i>indexed mesh</i> constructs the drawing index for a mesh.
 * @author Sarge
 */
public class IndexedMesh extends MutableMesh {
	private final List<Integer> indices = new ArrayList<>();
	private boolean restart;

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 */
	public IndexedMesh(Primitive primitive, List<Layout> layout) {
		super(primitive, layout);
	}

	@Override
	public int count() {
		return indices.size();
	}

	@Override
	protected int map(int vertex) {
		return indices.get(vertex);
	}

	/**
	 * Adds a vertex index to this mesh.
	 * @param index Vertex index
	 * @throws IndexOutOfBoundsException if {@link #index} is not a valid index for the vertices of this mesh
	 */
	public IndexedMesh add(int index) {
		if((index < 0) || (index >= super.count())) {
			throw new IndexOutOfBoundsException(index);
		}
		this.indices.add(index);
		return this;
	}

	/**
	 * Restarts the index.
	 * Note that an index containing one-or-more restarts cannot be compact.
	 * @see Index#isCompactIndex()
	 */
	public IndexedMesh restart() {
		indices.add(-1);
		restart = true;
		return this;
	}

	/**
	 * An <i>index</i> is a list of vertex indices.
	 * A {@link #isCompactIndex()} can optionally be written as {@link short} values via the {@link #compact()} method.
	 * @see #MAX_SHORT_INDEX_SIZE
	 */
	public interface Index extends MeshData {
		/**
		 * Maximum size of a {@code short} index.
		 */
		long MAX_SHORT_INDEX_SIZE = MathsUtility.unsignedMaximum(Short.SIZE);

		/**
		 * @return Whether the index for this mesh <b>can</b> be <i>compact</i>
		 * @see #compact()
		 * @see #MAX_SHORT_INDEX_SIZE
		 */
		boolean isCompactIndex();

		/**
		 * @return Compact index comprising {@code short} values rather than integers
		 * @throws IllegalStateException if this index cannot be compact
		 * @see #isCompactIndex()
		 */
		Index compact();
	}

	/**
	 * @return Index data
	 */
	public Index index() {
		return new DefaultIndex();
	}

	/**
	 * Default implementation for an integer index.
	 */
	private class DefaultIndex implements Index {
		@Override
		public final int length() {
			return count() * bytes();
		}

		/**
		 * @return Number of bytes per index element
		 */
		protected int bytes() {
			return Integer.BYTES;
		}

		@Override
		public void buffer(ByteBuffer buffer) {
			for(int n : indices) {
				buffer.putInt(n);
			}
		}

		@Override
		public boolean isCompactIndex() {
			return (indices.size() < MAX_SHORT_INDEX_SIZE) && !restart;
		}

		@Override
		public Index compact() {
			if(!isCompactIndex()) {
				throw new IllegalStateException("Index cannot be compact: " + this);
			}
			return new CompactIndex();
		}
	}

	/**
	 * Compact implementation for a short index.
	 */
	private class CompactIndex extends DefaultIndex {
		@Override
		protected int bytes() {
			return Short.BYTES;
		}

		@Override
		public void buffer(ByteBuffer buffer) {
			for(int n : indices) {
				buffer.putShort((short) n);
			}
		}

		@Override
		public boolean isCompactIndex() {
			return false;
		}

		@Override
		public Index compact() {
			throw new UnsupportedOperationException();
		}
	}
}
