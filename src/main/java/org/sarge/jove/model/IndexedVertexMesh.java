package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.Layout;
import org.sarge.jove.util.MathsUtility;

/**
 * An <i>indexed mesh</i> constructs an index buffer for a renderable mesh.
 * <p>
 * An index can be restarted using the {@link #restart()} method.
 * <p>
 * The index can be set as <i>compact</i> using the {@link #compact(boolean)} method.
 * This configures the index to be represented by {@code short} values when:
 * <ul>
 * <li>the length of the buffer is smaller than {@link #MAX_SHORT_INDEX_SIZE}</li>
 * <li>and the index does not contain any {@link #restart()} indices</li>
 * </ul>
 * The {@link #isCompactIndex()} indicates whether the index will be compacted when it is buffered.
 * <p>
 * @author Sarge
 */
public class IndexedVertexMesh extends VertexMesh implements IndexedMesh {
	/**
	 * Maximum size of a {@code short} index.
	 */
	public static final long MAX_SHORT_INDEX_SIZE = MathsUtility.unsignedMaximum(Short.SIZE);

	private final List<Integer> index = new ArrayList<>();
	private boolean compact;
	private int restart;

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 */
	public IndexedVertexMesh(Primitive primitive, List<Layout> layout) {
		super(primitive, layout);
	}

	@Override
	public int count() {
		return index.size();
	}

	@Override
	protected int map(int vertex) {
		return index.get(vertex);
	}

	/**
	 * Adds a vertex index to this mesh.
	 * @param index Vertex index
	 * @throws IndexOutOfBoundsException if {@link #index} is not a valid vertex index
	 */
	public IndexedVertexMesh add(int index) {
		if((index < 0) || (index >= super.count())) {
			throw new IndexOutOfBoundsException(index);
		}
		this.index.add(index);
		return this;
	}

	/**
	 * Restarts the index.
	 */
	public IndexedVertexMesh restart() {
		index.add(-1);
		++restart;
		return this;
	}

	@Override
	public boolean isCompactIndex() {
		return compact && (index.size() < MAX_SHORT_INDEX_SIZE) && (restart == 0);
	}

	/**
	 * Sets whether this index <i>can</i> be compacted.
	 * @param compact Whether to compact this index
	 * @see #isCompactIndex()
	 */
	public void compact(boolean compact) {
		this.compact = compact;
	}

	@Override
	public DataBuffer index() {
		return new DataBuffer() {
			@Override
			public int length() {
				final int size = isCompactIndex() ? Short.BYTES : Integer.BYTES;
				return count() * size;
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				if(isCompactIndex()) {
					for(int n : index) {
						buffer.putShort((short) n);
					}
				}
				else {
					for(int n : index) {
						buffer.putInt(n);
					}
				}
			}
		};
	}
}
