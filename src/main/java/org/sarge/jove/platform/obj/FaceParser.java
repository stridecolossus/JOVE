package org.sarge.jove.platform.obj;

import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate2D;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.platform.obj.ObjectModel.ComponentList;

/**
 * The <i>face parser</i> parses an OBJ face command.
 * <p>
 * TODO - doc format, assumptions, constraints
 * @author Sarge
 */
public class FaceParser implements Parser {
	@Override
	public void parse(String[] args, ObjectModel model) {
		// Update/validate face size
		model.init(args.length);

		// Parse face and add vertices
		for(String face : args) {
			// Tokenize face
			final String[] parts = face.trim().split("/");
			if(parts.length > 3) throw new IllegalArgumentException("Invalid face: " + face);

			// Add vertex position
			final Vertex.Builder vertex = new Vertex.Builder();
			final Point pos = lookup(model.vertices(), parts[0]);
			vertex.position(pos);

			// Add optional texture coordinate
			if(parts.length > 1) {
				final Coordinate2D coords = lookup(model.coordinates(), parts[1]);
				vertex.coords(coords);
			}

			// Add optional vertex normal
			if(parts.length == 3) {
				final Vector normal = lookup(model.normals(), parts[2]);
				vertex.normal(normal);
			}

			// Add vertex
			model.add(vertex.build());
		}
	}

	/**
	 * Helper - Looks up a component from the given list.
	 * @param <T> Component type
	 * @param list		List
	 * @param str 		Index string
	 * @return Specified component
	 * @see ComponentList#get(int)
	 */
	private static <T> T lookup(ComponentList<T> list, String index) {
		return list.get(Integer.parseInt(index.trim()));
	}
}
