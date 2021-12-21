package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;
import org.sarge.jove.util.IntegerList;

/**
 * A <i>mutable model</i> is used to construct vertex data and an optional index for a model.
 * <p>
 * Notes:
 * <ul>
 * <li>Buffers are generated on-demand</li>
 * <li>The vertex buffer is interleaved</li>
 * <li>Generated buffers are implemented as <b>direct</b> NIO buffers</li>
 * </ul>
 * <p>
 * The {@link #transform(List)} method is used to transform the vertex data and layout of a model.
 * <p>
 * @author Sarge
 */
public class MutableModel extends AbstractModel {
	protected final List<Vertex> vertices = new ArrayList<>();
	protected final IntegerList index = new IntegerList();

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 */
	public MutableModel(Primitive primitive, List<Layout> layout) {
		super(primitive, layout);
	}

	@Override
	public boolean isIndexed() {
		return index.size() > 0;
	}

	@Override
	public int count() {
		if(isIndexed()) {
			return index.size();
		}
		else {
			return vertices.size();
		}
	}

	/**
	 * @return Whether this model is empty
	 */
	public boolean isEmpty() {
		return vertices.isEmpty();
	}

	/**
	 * @return Vertices
	 */
	public Stream<Vertex> vertices() {
		return vertices.stream();
	}

	/**
	 * Adds a vertex.
	 * <p>
	 * Note that this method does not validate or make any assumptions regarding the components of the vertex.
	 * i.e. it is the responsibility of the user to ensure that vertices match the layout of this model.
	 * <p>
	 * @param v Vertex
	 */
	public MutableModel add(Vertex v) {
		vertices.add(notNull(v));
		return this;
	}

	/**
	 * @return Index
	 */
	public IntStream index() {
		return index.stream();
	}

	/**
	 * Adds an index.
	 * @param n Index
	 * @throws IllegalArgumentException if the index is invalid for this model
	 */
	public MutableModel add(int n) {
		if((n < 0) ||(n >= vertices.size())) throw new IllegalArgumentException(String.format("Invalid index: index=%d vertices=%d", n, vertices.size()));
		index.add(n);
		return this;
	}

	/**
	 * Transforms the layout of this model and <b>all</b> vertices to the given layout.
	 * <p>
	 * Example:
	 * <pre>
	 * // Create a model with the default vertex layout (all components)
	 * MutableModel model = new MutableModel(Primitive.TRIANGLES, Vertex.LAYOUT);
	 *
	 * // Add vertices
	 * Vertex vertex = Vertex.of(...);
	 * model.add(vertex);
	 * ...
	 *
	 * // Apply a transform to a re-ordered subset of the components
	 * model.transform(List.of(Colour.LAYOUT, Point.LAYOUT));
	 * </pre>
	 * <p>
	 * Note that the transformation compares layouts by <b>identity</b> to avoid matching different vertex components with the same layout, e.g. points and normals.
	 * <p>
	 * @param target Target layout
	 * @throws IllegalArgumentException if the current layout does not contain the target layout
	 * @throws ArrayIndexOutOfBoundsException for a vertex that does not match the new layout
	 * @see Vertex#transform(int[])
	 */
	public MutableModel transform(List<Layout> target) {
		// Init mapping from previous layout (comparing by identity)
		final Layout[] array = layout.toArray(Layout[]::new);
		final ToIntFunction<Layout> mapper = e -> {
			for(int n = 0; n < array.length; ++n) {
				if(e == array[n]) {
					return n;
				}
			}
			throw new IllegalArgumentException(String.format("Vertex component not present in model layout: layout=%s target=%s", layout, target));
		};

		// Build transform indices
		final int[] transform = target
				.stream()
				.mapToInt(mapper)
				.toArray();

		// Apply transform to vertex data
		for(Vertex v : vertices) {
			v.transform(transform);
		}

		// Update model layout
		layout.clear();
		layout.addAll(target);

		return this;
	}

	/**
	 * Iterates over the polygons of this model according to the drawing primitive.
	 * @return Polygon iterator
	 */
	public Iterator<int[]> iterator() {
		final int size = primitive.size();
		final int inc = primitive.isStrip() ? 1 : size;
		final int[] indices = new int[size];

		return new Iterator<>() {
			private int offset;

			public boolean hasNext() {
				return offset + size <= index.size();
			}

			public int[] next() {
				index.slice(offset, indices);
				offset += inc;
				return indices;
			}
		};
	}

	@Override
	public Bufferable vertexBuffer() {
		return new Bufferable() {
			private final int len = vertices.size() * Layout.stride(layout);

			@Override
			public int length() {
				return len;
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				for(Vertex v : vertices) {
					v.buffer(buffer);
				}
			}
		};
	}

	@Override
	public Bufferable indexBuffer() {
		return index.bufferable();
	}
}
