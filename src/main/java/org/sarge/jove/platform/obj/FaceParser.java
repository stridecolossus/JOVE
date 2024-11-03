package org.sarge.jove.platform.obj;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.jove.model.Vertex;

/**
 * The <i>face parser</i> parses an OBJ face command.
 * <p>
 * Each <i>face</i> is a polygon of <i>vertices</i> each comprising the following indices into the model:
 * <ul>
 * <li>vertex (or position)</li>
 * <li>texture coordinate</li>
 * <li>normal</li>
 * </ul>
 * <p>
 * Notes:
 * <ul>
 * <li>The normal and texture coordinate are optional</li>
 * <li>Indices start at <b>one</b> and can be negative</li>
 * <li><b>Constraint</b> This implementation assumes all faces are <b>triangles</b></li>
 * <li>The actual components of a vertex are not validated since the model layout is not known up-front</li>
 * </ul>
 * <p>
 * Face format:
 * <p>
 * Each face is specified as a slash-delimited tuple of indices.
 * The texture coordinate component is empty for the case where a face vertex is comprised of a vertex and normal.
 * <p>
 * Examples:
 * <pre>
 * f 1          // Simple vertex
 * f 1/2        // Vertex and texture coordinate
 * f 1/2/3      // Vertex, coordinate and normal
 * f 1//3       // Vertex and normal
 * </pre>
 * @see VertexComponentList
 * @author Sarge
 */
class FaceParser implements Parser {
	@Override
	public void parse(String args, ObjectModel model) {
		// Tokenize
		final String[] faces = args.split(" "); // StringUtils.split(args);
		if(faces.length != 3) {
			throw new IllegalArgumentException("Expected triangle face");
		}

		// Parse vertices for this face
		for(int n = 0; n < faces.length; ++n) {
			// Tokenize face
			final String[] parts = faces[n].split("/"); // StringUtils.splitPreserveAllTokens(faces[n], '/');
			if((parts.length == 0) || (parts.length > 3)) {
				throw new IllegalArgumentException("Invalid number of face components: " + parts.length);
			}

			// Parse vertex position
			final Point pos = parse(parts[0], model.positions());
			final var vertex = new MutableVertex(pos);
			model.add(vertex);

			// Parse optional texture coordinate
			if((parts.length > 1) && !parts[1].isEmpty()) {
				final Coordinate2D coord = parse(parts[1], model.coordinates());
				vertex.coord = coord;
			}

			// Parse optional vertex normal
			if(parts.length == 3) {
				final Normal normal = parse(parts[2], model.normals());
				vertex.normal = normal;
			}
		}
	}

	/**
	 * Parses a vertex component.
	 */
	private static <T> T parse(String value, VertexComponentList<T> list) {
		final int index = Integer.valueOf(value);
		return list.get(index);
	}

	/**
	 * Custom OBJ vertex with optional vertex normal and texture coordinate.
	 */
	private static class MutableVertex extends Vertex {
		private Normal normal;
		private Coordinate2D coord;

		/**
		 * Constructor.
		 * @param pos Vertex position
		 */
		MutableVertex(Point pos) {
			super(pos);
		}

		@Override
		public void buffer(ByteBuffer bb) {
			super.buffer(bb);
			if(normal != null) {
				normal.buffer(bb);
			}
			if(coord != null) {
				coord.buffer(bb);
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.position(), normal, coord);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof MutableVertex that) &&
					this.position().equals(that.position()) &&
					Objects.equals(this.normal, that.normal) &&
					Objects.equals(this.coord, that.coord);
		}
	}
}
