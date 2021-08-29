package org.sarge.jove.platform.obj;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Vertex;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.DefaultModel.Builder;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Primitive;
import org.sarge.lib.util.Check;

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
	private final Supplier<Builder> factory;
	private final Deque<Builder> builders = new LinkedList<>();

	/**
	 * Constructor.
	 * @param factory Factory for the model builder
	 */
	public ObjectModel(Supplier<Builder> factory) {
		this.factory = notNull(factory);
		add();
	}

	/**
	 * Constructor using a default model builder.
	 */
	public ObjectModel() {
		this(Builder::new);
	}

	/**
	 * @return Vertices
	 */
	List<Point> vertices() {
		return vertices;
	}

	/**
	 * @return Normals
	 */
	List<Vector> normals() {
		return normals;
	}

	/**
	 * @return Texture coordinates
	 */
	List<Coordinate> coordinates() {
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
	private Builder current() {
		return builders.getLast();
	}

	/**
	 * Adds a new model for the next group.
	 */
	private void add() {
		assert isEmpty();
		final Builder next = notNull(factory.get());
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
	 * Adds a vertex to the current model.
	 * <p>
	 * The <i>indices</i> parameter specifies the following indices:
	 * <ol>
	 * <li>position</li>
	 * <li>texture coordinate</li>
	 * <li>normal</li>
	 * </ol>
	 * Notes:
	 * <ul>
	 * <li>the vertex position is mandatory</li>
	 * <li>texture coordinate and normal are optional, i.e. can be {code null}</li>
	 * <li>indices start at <b>one</b> and can be negative</li>
	 * </ul>
	 * @param indices Vertex indices
	 * @throws IndexOutOfBoundsException for an invalid index
	 * @throws IllegalArgumentException if the array is empty or does not contain at least the vertex position
	 */
	public void vertex(Integer[] indices) {
		// Validate
		Check.notEmpty(indices);
		if(indices[0] == null) throw new IllegalArgumentException("Indices must contain a vertex position");

		// Add vertex position
		final var vb = new Vertex.Builder();
		vb.position(vertices.get(indices[0]));

		// Add optional texture coordinate
		if(indices[1] != null) {
			vb.coordinate(coords.get(indices[1]));
		}

		// Add optional normal
		if(indices[2] != null) {
			vb.normal(normals.get(indices[2]));
		}

		// Add vertex
		final Builder builder = current();
		builder.add(vb.build());
	}

	/**
	 * Constructs the model(s).
	 * @return Model(s)
	 * @throws IllegalArgumentException if the models cannot be constructed
	 */
	public Stream<Model> build() {
		return builders.stream().map(Builder::build);
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
