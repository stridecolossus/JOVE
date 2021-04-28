package org.sarge.jove.platform.obj;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Coordinate;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.DefaultModel;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;

/**
 * The <i>OBJ model</i> holds the transient vertex data during parsing and maintains the list of generated models.
 * @author Sarge
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

	// Data
	private final List<Point> vertices = new VertexComponentList<>();
	private final List<Vector> normals = new VertexComponentList<>();
	private final List<Coordinate> coords = new VertexComponentList<>();

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
	public List<Coordinate> coordinates() {
		return coords;
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

		// Reset transient model
		vertices.clear();
		normals.clear();
		coords.clear();

		// Start new model
		add();
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
