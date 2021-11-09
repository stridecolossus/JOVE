package org.sarge.jove.platform.obj;

/**
 * The <i>face parser</i> parses an OBJ face command.
 * <p>
 * A <i>face</i> is comprised of a set of <i>vertices</i> each consisting of the following indices into the model:
 * <ul>
 * <li>vertex (or position)</li>
 * <li>texture coordinate</li>
 * <li>normal</li>
 * </ul>
 * <p>
 * Notes:
 * <ul>
 * <li>The {@code normal} and {@code texture coordinate} are both optional</li>
 * <li>Indices start at <b>one</b></li>
 * <li>An index can also be <i>negative</i> relative to the current size of the model, e.g. -1 is the <i>last</i> element</li>
 * <li><b>Constraint</b> This implementation assumes all faces are <b>triangles</b></li>
 * </ul>
 * <p>
 * Face format:
 * <p>
 * Each face is specified as a slash-delimited tuple of indices.
 * Note that the {@code texture coordinate} is empty for the case where a face vertex is comprised of a vertex and normal.
 * <p>
 * Examples:
 * <pre>
 *  f 1			// Simple vertex
 *  f 1/2			// Vertex and texture coordinate
 *  f 1/2/3		// Vertex, coordinate and normal
 *  f 1//3		// Vertex and normal
 * </pre>
 * @see ObjectModel#position(Integer[])
 * @author Sarge
 */
class FaceParser implements Parser {
	@Override
	public void parse(String[] args, ObjectModel model) {
		// Validate face
		if(args.length != 4) {
			throw new IllegalArgumentException("Expected triangle face");
		}

		// Parse vertices for this face
		for(int n = 0; n < 3; ++n) {
			// Tokenize face
			final String face = args[n + 1];
			final String[] parts = face.split("/");
			Parser.trim(parts);
			if(parts.length > 3) throw new IllegalArgumentException("Invalid face: " + face);

			// Parse mandatory vertex position
			final int v = Integer.parseInt(parts[0]);

			// Parse optional texture coordinate
			Integer vt = null;
			if((parts.length > 1) && !parts[1].isEmpty()) {
				vt = Integer.parseInt(parts[1]);
			}

			// Parse optional normal
			Integer vn = null;
			if(parts.length == 3) {
				vn = Integer.parseInt(parts[2]);
			}

			// Add vertex to model
			model.vertex(v, vn, vt);
		}
	}
}
