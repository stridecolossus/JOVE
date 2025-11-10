package org.sarge.jove.model;

import java.nio.*;
import java.util.*;

import org.sarge.jove.common.Layout;
import org.sarge.jove.util.MathsUtility;

/**
 * An <i>indexed mesh</i> constructs an index buffer for a renderable mesh.
 * <p>
 * The index can be configured to use {@code short} indices if the length of the buffer is smaller than {@link #MAX_SHORT_INDEX_SIZE}.
 * <p>
 * An index can be restarted using the {@link #restart()} method.
 * <p>
 * @author Sarge
 */
public class IndexedMesh extends MutableMesh {
	/**
	 * Maximum size of a {@code short} index.
	 */
	public static final long MAX_SHORT_INDEX_SIZE = MathsUtility.unsignedMaximum(Short.SIZE);

	private final List<Integer> index = new ArrayList<>();
	private int restart;
	private boolean compact;

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
		return index.size() - restart;
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
	public IndexedMesh add(int index) {
		if((index < 0) || (index >= super.count())) {
			throw new IndexOutOfBoundsException(index);
		}
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
	 * Sets whether the index buffer is <i>compact<i> and buffered with {@code short} vertex indices.
	 * @see #index()
	 */
	public IndexedMesh compact(boolean compact) {
		this.compact = compact;
		return this;
	}

	/**
	 * {@inheritDoc}
	 * @throws IllegalStateException if this index is {@link #compact(boolean)} but is too large or contains {@link #restart()} commands
	 */
	@Override
	public Optional<ByteBuffer> index() {
		// Allocate buffer
		final int size = compact ? Short.BYTES : Integer.BYTES;
		final var buffer = ByteBuffer.allocateDirect(index.size() * size).order(ByteOrder.nativeOrder());

		// Write buffer
		if(compact) {
			if(index.size() > MAX_SHORT_INDEX_SIZE) {
				throw new IllegalStateException("Index is too large to be compact");
			}
			if(restart > 0) {
				throw new IllegalStateException("Cannot restart a compact index");
			}

			final var adapter = buffer.asShortBuffer();
    		for(int n : index) {
    			adapter.put((short) n);
    		}
		}
		else {
    		final var adapter = buffer.asIntBuffer();
    		for(int n : index) {
    			adapter.put(n);
    		}
		}
		buffer.rewind();

		return Optional.of(buffer);
	}
}
