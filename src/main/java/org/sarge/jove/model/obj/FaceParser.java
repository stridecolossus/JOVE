package org.sarge.jove.model.obj;

import java.util.Scanner;

import org.sarge.jove.common.TextureCoordinate;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex;

/**
 * Parser for a face polygon.
 * @author Sarge
 */
public class FaceParser implements Parser {
	@Override
	public void parse(Scanner scanner, ObjectModel model) {
		// Tokenize face entries
		final String line = scanner.nextLine();
		final String[] faces = line.split(" ");

		// Parse each face and add to model
		for(String face : faces) {
			parse(face, model);
		}
	}

	/**
	 * Parse a face entry.
	 * @param face		Face
	 * @param model		Model
	 */
	private static void parse(String face, ObjectModel model) {
		// Tokenize face components
		final String[] parts = face.split("/");

		// Lookup vertex position
		final int idx = Integer.parseInt(parts[0]);
		final Point pos = model.getVertex(idx);

		// Add vertex to model
		final Vertex v = new Vertex(pos);
		model.getGroup().add(v);

		// Add optional texture coordinates
		if(parts.length > 1) {
			if(parts[1].length() > 0) {
				final int coordsIndex = Integer.parseInt(parts[1]);
				final TextureCoordinate coords = model.getTextureCoord(coordsIndex);
				v.setTextureCoords(coords);
			}
		}

		// Add normal
		if(parts.length == 3) {
			final int normalIndex = Integer.parseInt(parts[2]);
			final Vector normal = model.getNormal(normalIndex);
			v.setNormal(normal);
		}
	}
}
