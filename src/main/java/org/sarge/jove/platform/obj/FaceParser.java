package org.sarge.jove.platform.obj;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.geometry.*;
import org.sarge.jove.model.*;

/**
 * The <i>face parser</i> parses an OBJ face command.
 * <p>
 * Each <i>face</i> is a polygon of <i>vertices</i> comprising the following components:
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
	private final ObjectModel model;

	/**
	 * Constructor.
	 * @param model OBJ model
	 */
	public FaceParser(ObjectModel model) {
		this.model = requireNonNull(model);
	}

	@Override
	public void parse(String[] tokens) {
		for(int n = 0; n < 3; ++n) {
			// Split face
			final String[] components = tokens[n + 1].split("/");
			if((components.length < 1) || (components.length > 3)) {
				throw new IllegalArgumentException("Invalid number of face vertices");
			}

			// Parse vertex position
			final Point pos = parse(components[0], model.positions());
			final Vertex vertex = new Vertex(pos);

			// Parse optional normal
			if(components.length == 3) {
				final Normal normal = parse(components[2], model.normals());
				vertex.add(normal);
			}

			// Parse optional texture coordinate
			if((components.length > 1) && !components[1].isEmpty()) {
				final Coordinate coordinate = parse(components[1], model.coordinates());
				vertex.add(coordinate);
			}

			// Add vertex to model
			model.add(vertex);
		}
	}

	private static <T> T parse(String value, VertexComponentList<T> list) {
		final int index = Integer.parseInt(value);
		return list.get(index);
	}
}
