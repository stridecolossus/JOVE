package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.util.BitField;

/**
 * An <i>indexed mesh</i> constructs an index buffer for a renderable mesh.
 * <p>
 * Notes:
 * <ul>
 * <li>The index buffer can be configured to use the smallest data type depending on the size of the index, see {@link #compact(boolean)}</li>
 * <li>The index can be restarted using the {@link #restart()} method</i>
 * </ul>
 * <p>
 * @author Sarge
 */
public class IndexedMesh extends DefaultMesh {
	/**
	 * Size of a {@code short} index.
	 */
	private static final long SHORT_INDEX = BitField.unsignedMaximum(Short.SIZE);

	/**
	 * Determines whether the given draw count requires an {@code int} index.
	 * i.e. Whether the number of indices is larger than {@linkplain #SHORT_INDEX}.
	 * TODO - better doc, maybe return data type?
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
	public IndexedMesh(Primitive primitive, CompoundLayout layout) {
		super(primitive, layout);
	}

	@Override
	public final int count() {
		return index.size() - restart;
	}

	@Override
	public final boolean isIndexed() {
		return true;
	}

	/**
	 * @return Index
	 */
	public IntStream index() {
		return index.stream().mapToInt(Integer::intValue);
	}

	@Override
	protected final Stream<int[]> triangles() {
		return super.triangles().map(this::map);
	}

	private int[] map(int[] indices) {
		for(int n = 0; n < indices.length; ++n) {
			indices[n] = index.get(n);
		}
		return indices;
	}

	/**
	 * Adds a vertex index to this mesh.
	 * @param index Vertex index
	 * @throws IndexOutOfBoundsException if {@link #index} is not a valid vertex index
	 */
	public IndexedMesh add(int index) {
		if((index < 0) || (index >= super.count())) throw new IndexOutOfBoundsException(index);
		this.index.add(index);
		return this;
	}

	/**
	 * Restarts the index.
	 */
	public IndexedMesh restart() {
		index.add(-1);
		++restart;
		return this;
	}

	/**
	 * Sets whether the index buffer uses the most <i>compact</i> data type (default is {@code true}).
	 * <p>
	 * If this index is {@link #compact} and the number of indices is small enough the data type of the index buffer is {@code short}.
	 * Otherwise the index buffer is comprised of {@code int} indices.
	 * Note that an index that includes {@link #restart()} cannot be compact.
	 * <p>
	 * @param compact Whether to use compact indices
	 * @see #isIntegerIndex(int)
	 */
	public IndexedMesh compact(boolean compact) {
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
					final int[] indices = index.stream().mapToInt(Integer::intValue).toArray();
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
	public final Optional<ByteSizedBufferable> indexBuffer() {
		return Optional.of(new IndexBuffer());
	}

	@Override
	public final BufferedMesh buffer() {
		return new BufferedMesh(this, new VertexBuffer(), new IndexBuffer());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("compact", compact)
				.append("restart", restart)
				.build();
	}
}
