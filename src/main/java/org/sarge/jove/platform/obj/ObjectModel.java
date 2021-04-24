package org.sarge.jove.platform.obj;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Coordinate.Coordinate2D;
import org.sarge.jove.common.Component;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.DefaultModel;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.model.Vertex.Layout;

/**
 * The <i>OBJ model</i> holds the transient vertex data during parsing and maintains the list of generated models.
 * <p>
 * TODO
 */
public class ObjectModel {
	/**
	 * List of OBJ vertex components that can also be retrieved using negative indices.
	 */
	static class VertexComponentList<T> extends ArrayList<T> {
		@Override
		public T get(int index) {
			if(index > 0) {
				return super.get(index - 1);
			}
			else
			if(index < 0) {
				return super.get(size() + index);
			}
			else {
				throw new IndexOutOfBoundsException("Invalid zero index");
			}
		}
	}

	/**
	 * Special case component list that optionally flips texture coordinates.
	 */
	private static class FlipTextureComponentList extends VertexComponentList<Coordinate2D> {
		private boolean flip = true;

		@Override
		public boolean add(Coordinate2D coords) {
			if(flip) {
				return super.add(new Coordinate2D(coords.u(), -coords.v()));
			}
			else {
				return super.add(coords);
			}
		}
	}

	// Data
	private final List<Point> vertices = new VertexComponentList<>();
	private final List<Vector> normals = new VertexComponentList<>();
	private final FlipTextureComponentList coords = new FlipTextureComponentList();

	// Models
	private final Supplier<DefaultModel.Builder> factory;
	private final Deque<DefaultModel.Builder> builders = new LinkedList<>();

	/**
	 * Constructor.
	 * @param factory Factory for the model builder
	 */
	public ObjectModel(Supplier<DefaultModel.Builder> factory) {
		this.factory = notNull(factory);
		add();
	}

	/**
	 * Constructor using a default model builder.
	 */
	public ObjectModel() {
		this(DefaultModel.Builder::new);
	}

	/**
	 * @return Vertices
	 */
	public List<Point> vertices() {
		return vertices;
	}

	/**
	 * @return Normals
	 */
	public List<Vector> normals() {
		return normals;
	}

	/**
	 * @return Texture coordinates
	 */
	public List<Coordinate2D> coordinates() {
		return coords;
	}

	/**
	 * Sets whether to vertically flip texture coordinates.
	 * @param flip Whether to flip coordinates (default is {@code true})
	 */
	public void setFlipTextureCoordinates(boolean flip) {
		coords.flip = flip;
	}

	/**
	 * @return Whether the current object group is empty
	 */
	public boolean isEmpty() {
		return vertices.isEmpty();
	}

	/**
	 * @return Builder for the current object group
	 */
	private DefaultModel.Builder current() {
		return builders.getLast();
	}

	/**
	 * Adds a new model for the next group.
	 */
	private void add() {
		assert isEmpty();
		final DefaultModel.Builder next = notNull(factory.get());
		next.primitive(Primitive.TRIANGLES);
		next.clockwise(true);
		builders.add(next);
	}

	/**
	 * Starts a new object group.
	 */
	public void start() {
		// Ignore if the current group is empty
		if(isEmpty()) {
			return;
		}

		// Initialise the vertex layout for the previous model
		init();

		// Reset transient model
		vertices.clear();
		normals.clear();
		coords.clear();

		// Start new model
		add();
	}

	/**
	 * Sets the name of the current object.
	 * @param name Object name
	 */
	public void name(String name) {
		// TODO
//		final VertexModel.Builder builder = current();
//		builder.name(name);
	}

	/**
	 * Initialises the model layout.
	 */
	private void init() {
		// Determine vertex layout for the current object group
		final var layout = new ArrayList<Component>();
		layout.add(Layout.POSITION);
		if(!normals.isEmpty()) {
			layout.add(Layout.NORMAL);
		}
		if(!coords.isEmpty()) {
			layout.add(Layout.COORDINATE);
		}

		// Initialise current model
		final DefaultModel.Builder builder = current();
		builder.layout(new Vertex.Layout(layout));
	}

	/**
	 * Adds a face vertex to the current model.
	 * @param v			Vertex index
	 * @param n			Optional normal index
	 * @param tc		Optional texture coordinates index
	 * @throws IndexOutOfBoundsException if any index is not valid for the current group
	 */
	public void vertex(int v, Integer n, Integer tc) {
		// Build vertex
		final var vertex = new Vertex.Builder();
		vertex.position(vertices.get(v));

		// Add optional normal
		if(n != null) {
			vertex.normal(normals.get(n));
		}

		// Add optional texture coordinate
		if(tc != null) {
			vertex.coordinate(coords.get(tc));
		}

		// Add to model
		final DefaultModel.Builder builder = current();
		builder.add(vertex.build());
	}

	/**
	 * Constructs the model(s).
	 * @return Model(s)
	 * @throws IllegalArgumentException if the models cannot be constructed
	 */
	public Stream<Model> build() {
		init();
		return builders.stream().map(DefaultModel.Builder::build);
	}
	// TODO - assume one model at a time, factor out building per model, this class (or maybe loader?) should be meta-model that returns the resultant list

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("vertices", vertices.size())
				.append("normals", normals.size())
				.append("coords", coords.size())
				.append("models", builders.size())
				.build();
	}
}
