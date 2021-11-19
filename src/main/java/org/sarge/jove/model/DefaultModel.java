package org.sarge.jove.model;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.sarge.jove.common.Layout;
import org.sarge.jove.io.Bufferable;
import org.sarge.jove.model.Model.AbstractModel;

/**
 * A <i>default model</i> is comprised of vertices and an optional index buffer.
 * <p>
 * Notes:
 * <ul>
 * <li>Buffers are generated on-demand</li>
 * <li>The vertex buffer is interleaved</li>
 * <li>Generated buffers are implemented as direct NIO buffers</li>
 * </ul>
 * <p>
 * @author Sarge
 */
public class DefaultModel extends AbstractModel {
	/**
	 * Creates a default model.
	 * @param header		Header
	 * @param vertices		Vertex data
	 * @param index			Optional index
	 * @return New default model
	 */
	public static DefaultModel of(Header header, List<Vertex> vertices, int[] index) {
		if(index == null) {
			return new DefaultModel(header, List.copyOf(vertices), null);
		}
		else {
			return new DefaultModel(header, List.copyOf(vertices), Arrays.copyOf(index, index.length));
		}
	}
	// TODO - extend IndexedModel and implementations for Integer and Short? see type in VulkanBuffer

	private final List<Vertex> vertices;
	private final int[] index;

	/**
	 * Constructor.
	 * @param header			Model header
	 * @param vertices			Vertices
	 * @param index				Optional index
	 */
	protected DefaultModel(Header header, List<Vertex> vertices, int[] index) {
		super(header);
		this.vertices = notNull(vertices);
		this.index = index;
	}

	@Override
	public boolean isIndexed() {
		return index != null;
	}

	@Override
	public Bufferable vertices() {
		return new Bufferable() {
			private final int len = vertices.size() * Layout.stride(header.layout());

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
	// TODO - non-interleaved option? i.e. would have to select each component from each vertex

	@Override
	public Optional<Bufferable> index() {
		if(index == null) {
			return Optional.empty();
		}

		final Bufferable buffer = new Bufferable() {
			private final int len = index.length * Integer.BYTES;

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
					buffer.put(index);
				}
			}
		};
		return Optional.of(buffer);
	}

	@Override
	public DefaultModel transform(List<Layout> layouts) {
		// Build mapping to new layout
		final int map[] = layouts
				.stream()
				.mapToInt(this::indexOf)
				.toArray();

		// Transform vertex data
		final List<Vertex> data = vertices
				.stream()
				.map(v -> v.transform(map))
				.collect(toList());

		// Create transformed model
		final Header prev = this.header();
		final Header header = new Header(layouts, prev.primitive(), prev.count());
		return new DefaultModel(header, data, index);
	}

	/**
	 * Looks up the index of the given layout in this model.
	 * @param layout Layout
	 * @return Index
	 * @throws IllegalArgumentException if the layout is not present
	 */
	private int indexOf(Layout layout) {
		final int index = header.layout().indexOf(layout);
		if(index == -1) throw new IllegalArgumentException(String.format("Invalid layout for this model: layout=%s model=%s", layout, this));
		return index;
	}
}
