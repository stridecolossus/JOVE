package org.sarge.jove.platform.obj;

import org.apache.commons.lang3.StringUtils;

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
 * @see VertexComponentList#get(int)
 * @see ObjectModel#vertex(int[])
 * @author Sarge
 */
class FaceParser implements Parser {
	@Override
	public void parse(String args, ObjectModel model) {
		// Tokenize
		final String[] faces = StringUtils.split(args);
		if(faces.length != 3) {
			throw new IllegalArgumentException("Expected triangle face");
		}

		// Parse vertices for this face
		for(int n = 0; n < faces.length; ++n) {
			// Tokenize face
			final String[] parts = StringUtils.splitPreserveAllTokens(faces[n], '/');
			if((parts.length == 0) || (parts.length > 3)) {
				throw new IllegalArgumentException("Invalid number of face components: expected=%d actual=%d".formatted(model.components(), parts.length));
			}

			// Parse mandatory vertex position
			final int[] components = new int[3];
			components[0] = Integer.parseInt(parts[0]);

			// Parse optional texture coordinate
			if((parts.length > 1) && !parts[1].isEmpty()) {
				components[1] = Integer.parseInt(parts[1]);
			}

			// Parse optional vertex normal
			if(parts.length == 3) {
				components[2] = Integer.parseInt(parts[2]);
			}

			// Add vertex
			model.vertex(components);
		}
	}
}
