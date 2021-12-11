package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Layout;
import org.sarge.jove.model.Vertex.Component;
import org.sarge.lib.util.Check;

/**
 * A <i>model builder</i> is used to construct a vertex model.
 * @see Vertex
 * @author Sarge
 */
public class ModelBuilder {
	private Primitive primitive = Primitive.TRIANGLE_STRIP;
	private final List<Layout> layout = new ArrayList<>();
	private boolean normals;

	protected final List<Vertex> vertices = new ArrayList<>();
	protected final List<Integer> index = new ArrayList<>();

	/**
	 * @return Whether the vertex data of this model is empty
	 */
	public boolean isEmpty() {
		return vertices.isEmpty();
	}

	/**
	 * Sets the drawing primitive (default is {@link Primitive#TRIANGLE_STRIP}).
	 * @param primitive Drawing primitive
	 */
	public ModelBuilder primitive(Primitive primitive) {
		this.primitive = notNull(primitive);
		return this;
	}

	/**
	 * Adds a vertex component to the layout of this model.
	 * @param layout Vertex component layout
	 */
	public ModelBuilder layout(Layout layout) {
		this.layout.add(notNull(layout));
		return this;
	}

	/**
	 * Adds the given list of a vertex components to the layout of this model.
	 * @param layout Vertex component layout
	 */
	public ModelBuilder layout(List<Component> components) {
		for(Component c : components) {
			layout(c.layout());
		}
		return this;
	}

	/**
	 * Adds a vertex to this model.
	 * @param vertex Vertex
	 */
	public ModelBuilder add(Vertex vertex) {
		vertices.add(notNull(vertex));
		return this;
	}

	/**
	 * Adds an index to this model.
	 * @param n Index
	 * @throws IllegalArgumentException if the index is larger than the number of vertices
	 */
	public ModelBuilder add(int n) {
		Check.zeroOrMore(n);
		if(n >= vertices.size()) throw new IllegalArgumentException(String.format("Invalid index: index=%d vertices=%d", n, vertices.size()));
		index.add(n);
		return this;
	}

	/**
	 * Constructs this model.
	 * @return New model
	 * @throws IllegalArgumentException if the model is not valid
	 * @see AbstractModel#validate(boolean)
	 */
	public Model build() {
		final AbstractModel model = new DefaultModel(primitive, layout, vertices, index);
		model.validate(normals);
		return model;
	}
}
