package org.sarge.jove.model;

import static java.util.stream.Collectors.toList;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;
import org.sarge.jove.model.Vertex.Component;

/**
 * A <i>default model</i> is comprised of vertices and an optional index buffer.
 * <p>
 * Notes:
 * <ul>
 * <li>Buffers are generated on-demand</li>
 * <li>The vertex buffer is interleaved</li>
 * <li>Generated buffers are implemented as <b>direct</b> NIO buffers</li>
 * </ul>
 * <p>
 * The {@link #transform(List)} method is used to transform the vertex component layout of a model.
 * <p>
 * @author Sarge
 */
public class DefaultModel extends AbstractModel {
	private final List<Vertex> vertices;
	private final List<Integer> index;

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 * @param vertices		Vertices
	 * @param index			Index
	 */
	public DefaultModel(Primitive primitive, List<Layout> layout, List<Vertex> vertices, List<Integer> index) {
		super(primitive, layout);
		this.vertices = List.copyOf(vertices);
		this.index = List.copyOf(index);
		validate(false);
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
	 * Transforms the vertices of this model to the given component layout.
	 * @param components Vertex component layout
	 * @return Transformed model
	 */
	public DefaultModel transform(List<Component> components) {
		// Ignore if same layout
		final List<Layout> layout = components.stream().map(Component::layout).collect(toList());
		if(layout.equals(this.layout())) {
			return this;
		}

		// Transform vertices and create new model
		final List<Vertex> transformed = vertices.stream().map(v -> v.transform(components)).collect(toList());
		return new DefaultModel(primitive(), layout, transformed, index);
	}

	/**
	 * Transforms the vertices of this model to the given component layout.
	 * @param components Vertex component layout
	 * @return Transformed model
	 */
	public DefaultModel transform(Component... components) {
		return transform(Arrays.asList(components));
	}

	@Override
	public Bufferable vertices() {
		return new Bufferable() {
			private final int len = vertices.size() * Layout.stride(layout());

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
	public Bufferable index() {
		return new Bufferable() {
			private final int len = index.size() * Integer.BYTES;

			@Override
			public int length() {
				return len;
			}

			@Override
			public void buffer(ByteBuffer bb) {
				final IntBuffer buffer = bb.asIntBuffer();
				if(buffer.isDirect()) {
					for(int n : index) {
						buffer.put(n);
					}
				}
				else {
					final int[] array = index.stream().mapToInt(Integer::intValue).toArray();
					buffer.put(array);
				}
			}
		};
	}
}
