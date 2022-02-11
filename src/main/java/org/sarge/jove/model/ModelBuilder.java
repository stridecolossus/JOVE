package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Layout;

/**
 * A <i>mutable model</i> is used to construct vertex data and an optional index for a model.
 * <p>
 * Notes:
 * <ul>
 * <li>Buffers are generated on-demand</li>
 * <li>The vertex buffer is interleaved</li>
 * <li>Generated buffers are implemented as <b>direct</b> NIO buffers</li>
 * </ul>
 * @author Sarge
 */
public class ModelBuilder {
	private Primitive primitive = Primitive.TRIANGLE_STRIP;
	private final List<Layout> layout = new ArrayList<>();
	private final List<Vertex> vertices = new ArrayList<>();
	private final List<Integer> index = new ArrayList<>();

	/**
	 * Sets the drawing primitive for this model (default is {@link Primitive#TRIANGLE_STRIP}).
	 * @param primitive Drawing primitive
	 */
	public ModelBuilder primitive(Primitive primitive) {
		this.primitive = notNull(primitive);
		return this;
	}

	/**
	 * Adds a vertex layout to this model.
	 * @param layout Vertex layout
	 */
	public ModelBuilder layout(Layout layout) {
		this.layout.add(notNull(layout));
		return this;
	}

	/**
	 * Helper - Adds multiple vertex layouts to this model.
	 * @param layouts Vertex layouts
	 */
	public ModelBuilder layout(List<Layout> layouts) {
		this.layout.addAll(layouts);
		return this;
	}

	/**
	 * Adds a vertex.
	 * <p>
	 * Note that this method does not validate or make any assumptions regarding the components of the vertex.
	 * i.e. it is the responsibility of the user to ensure that vertices match the layout of this model.
	 * <p>
	 * @param v Vertex
	 */
	public ModelBuilder add(Vertex v) {
		vertices.add(notNull(v));
		return this;
	}

	/**
	 * Adds an index.
	 * @param n Index
	 * @throws IllegalArgumentException if the index is invalid for this model
	 */
	public ModelBuilder add(int n) {
		if((n < 0) ||(n >= vertices.size())) throw new IllegalArgumentException(String.format("Invalid index: index=%d vertices=%d", n, vertices.size()));
		index.add(n);
		return this;
	}

	/**
	 * Adds a primitive restart index.
	 */
	public ModelBuilder restart() {
		index.add(-1);
		return this;
	}

	/**
	 * Constructs this model.
	 * @return New model
	 */
	public DefaultModel build() {
		return new DefaultModel(primitive, layout, vertices, index);
	}
}
