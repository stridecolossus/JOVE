package org.sarge.jove.platform.obj;

import org.apache.commons.lang3.StringUtils;

/**
 * The <i>face parser</i> parses the OBJ face command.
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
 * @see ObjectModel#position(Integer[])
 * @see VertexComponentList#get(int)
 * @author Sarge
 */
class FaceParser implements Parser {
	private static final int SIZE = 3;

	@Override
	public void parse(String args, ObjectModel model) {
		// Tokenize
		final String[] faces = StringUtils.split(args);
		if(faces.length != SIZE) {
			throw new IllegalArgumentException("Expected triangle face");
		}

		// Parse vertices for this face
		for(int n = 0; n < faces.length; ++n) {
			// Tokenize face
			final String face = faces[n];
			final String[] parts = StringUtils.splitPreserveAllTokens(face, '/');
			if(parts.length > SIZE) throw new IllegalArgumentException("Invalid face: " + face);

			// Parse mandatory vertex position
			if((parts.length == 0) || parts[0].isEmpty()) throw new IllegalArgumentException("Missing mandatory vertex position: " + face);
			final int v = Integer.parseInt(parts[0]);

			// Parse optional texture coordinate
			Integer vt = null;
			if((parts.length > 1) && !parts[1].isEmpty()) {
				vt = Integer.parseInt(parts[1]);
			}

			// Parse optional normal
			Integer vn = null;
			if(parts.length == SIZE) {
				vn = Integer.parseInt(parts[2]);
			}

			// Add vertex to model
			model.vertex(v, vn, vt);
		}
	}
}
