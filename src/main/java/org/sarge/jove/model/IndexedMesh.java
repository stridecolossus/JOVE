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
	public IndexedMesh(Primitive primitive, Layout... layout) {
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
		indices.add(index);
		return this;
	}

	/**
	 * Restarts the index.
	 * Note that an index containing one-or-more restarts can <b>only</b> be represented by 32-bit values.
	 * @see Index#minimumElementBytes()
	 */
	public IndexedMesh restart() {
		indices.add(-1);
		restart = true;
		return this;
	}

	@Override
	public Optional<Index> index() {
		return Optional.of(new IntegerIndex());
	}

	/**
	 * Default 32-bit index.
	 */
	private class IntegerIndex implements Index {
		@Override
		public final int length() {
			return indices.size() * bytes();
		}

		/**
		 * @return Bytes per element
		 */
		protected int bytes() {
			return Integer.BYTES;
		}

		@Override
		public void buffer(ByteBuffer buffer) {
			for(int n : indices) {
				buffer.putInt(n);
			}
			// TODO
			assert buffer.remaining() == 0;
		}

		@Override
		public final int minimumElementBytes() {
			// An restarted index can only be represented by 32-bit indices
			if(restart) {
				return Integer.BYTES;
			}

			// Determine smallest element size for the index
			final int vertices = IndexedMesh.super.count();
			if(vertices <= MathsUtility.unsignedMaximum(Byte.SIZE)) {
				return Byte.BYTES;
			}
			else
			if(vertices <= MathsUtility.unsignedMaximum(Short.SIZE)) {
				return Short.BYTES;
			}
			else {
				return Integer.BYTES;
			}
		}

		@Override
		public Index index(int bytes) {
			final int min = minimumElementBytes();
			if(bytes < min) {
				throw new IllegalArgumentException("Element size %d too small for minimum %d".formatted(bytes, min));
			}

			return switch(bytes) {
				case Byte.BYTES		-> new ByteIndex();
				case Short.BYTES	-> new ShortIndex();
				case Integer.BYTES	-> this;
				default				-> throw new IllegalArgumentException("Unsupported element size: " + bytes);
			};
		}
	}

	/**
	 * Index of 16-bit values.
	 */
	class ShortIndex extends IntegerIndex {
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
	}

	/**
	 * Index of 8-bit values.
	 */
	class ByteIndex extends IntegerIndex {
		@Override
		protected int bytes() {
			return Byte.BYTES;
		}

		@Override
		public void buffer(ByteBuffer buffer) {
			for(int n : indices) {
				buffer.put((byte) n);
			}
		}
	}
}
