package org.sarge.jove.platform.obj;

import static org.sarge.jove.util.Check.notNull;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate2D;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;

/**
 * The <i>OBJ model</i> holds the transient vertex data during parsing and maintains the list of generated models.
 * <p>
 * TODO
 */
public class ObjectModel {
	/**
	 * A <i>component list</i> is a mutable list of vertex components.
	 * @param <T> Component type
	 */
	public static class ComponentList<T> {
		protected final List<T> list = new ArrayList<>();

		/**
		 * @return Size of this list
		 */
		public int size() {
			return list.size();
		}

		/**
		 * Adds an object to this list.
		 * @param obj Object to add
		 */
		public void add(T obj) {
			list.add(notNull(obj));
		}

		/**
		 * Retrieves an object from this list by index.
		 * @param index 1..n or negative from the <b>end</b> of the list
		 * @return Specified object
		 * @throws IndexOutOfBoundsException if the index is zero or is out-of-bounds for this list
		 */
		public T get(int index) {
			if(index > 0) {
				return list.get(index - 1);
			}
			else if(index < 0) {
				return list.get(list.size() + index);
			}
			else {
				throw new IndexOutOfBoundsException("Invalid zero index");
			}
		}

		@Override
		public String toString() {
			return String.valueOf(list.size());
		}
	}

	/**
	 * Special case component list that optionally flips texture coordinates.
	 */
	private static class FlipTextureComponentList extends ComponentList<Coordinate2D> {
		private boolean flip;

		@Override
		public void add(Coordinate2D coords) {
			if(flip) {
				super.add(new Coordinate2D(coords.u, -coords.v));
			}
			else {
				super.add(coords);
			}
		}
	}

	// Data
	private final ComponentList<Point> vertices = new ComponentList<>();
	private final ComponentList<Vector> normals = new ComponentList<>();
	private final FlipTextureComponentList coords = new FlipTextureComponentList();

	// Models
	private final Supplier<Model.Builder> factory;
	private final Deque<Model.Builder> builders = new LinkedList<>();

	/**
	 * Constructor.
	 * @param factory Factory for the model builder
	 */
	public ObjectModel(Supplier<Model.Builder> factory) {
		this.factory = notNull(factory);
		add();
	}

	/**
	 * Constructor using a default model builder.
	 */
	public ObjectModel() {
		this(Model.Builder::new);
	}

	/**
	 * @return Vertices
	 */
	public ComponentList<Point> vertices() {
		return vertices;
	}

	/**
	 * @return Normals
	 */
	public ComponentList<Vector> normals() {
		return normals;
	}

	/**
	 * @return Texture coordinates
	 */
	public ComponentList<Coordinate2D> coordinates() {
		return coords;
	}

	/**
	 * Sets whether to vertically flip texture coordinates.
	 * @param flip Whether to flip coordinates (default is {@code false})
	 */
	public void setFlipTextureCoordinates(boolean flip) {
		coords.flip = flip;
	}

	/**
	 * @return Builder for the current object group
	 */
	private Model.Builder current() {
		return builders.getLast();
	}

	/**
	 * @return Whether the current model is empty
	 */
	public boolean isEmpty() {
		return vertices.list.isEmpty();
	}

	/**
	 * Adds a new model for the next group.
	 */
	private void add() {
		assert isEmpty();
		final Model.Builder next = notNull(factory.get());
		next.primitive(Primitive.TRIANGLES);
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
		vertices.list.clear();
		normals.list.clear();
		coords.list.clear();

		// Start new model
		add();
	}

	/**
	 * Initialises the model layout.
	 */
	private void init() {
		// Determine vertex layout for the current object group
		final var layout = new ArrayList<Vertex.Component>();
		layout.add(Vertex.Component.POSITION);
		if(!normals.list.isEmpty()) {
			layout.add(Vertex.Component.NORMAL);
		}
		if(!coords.list.isEmpty()) {
			layout.add(Vertex.Component.TEXTURE_COORDINATE);
		}

		// Initialise current model
		final Model.Builder builder = current();
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
			vertex.coords(coords.get(tc));
		}

		// Add to model
		final Model.Builder builder = current();
		builder.add(vertex.build());
	}

	/**
	 * Constructs the model(s).
	 * @return Model(s)
	 * @throws IllegalStateException if the model is empty
	 * @throws IllegalArgumentException if the models cannot be constructed
	 */
	public Stream<Model> build() {
		if(isEmpty()) throw new IllegalStateException("Model is empty");
		init();
		return builders.stream().map(Model.Builder::build);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("vertices", vertices)
				.append("normals", normals)
				.append("coords", coords)
				.append("models", builders.size())
				.build();
	}
}
