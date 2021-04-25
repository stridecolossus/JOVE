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
 * @see ObjectModel#vertex(int, Integer, Integer)
 * @author Sarge
 */
public class FaceParser implements Parser {
	/**
	 * @throws IllegalArgumentException for an invalid or non-triangular face
	 */
	@Override
	public void parse(String[] args, ObjectModel model) {
		// Validate face
		if(args.length != 3) {
			throw new IllegalArgumentException("Expected triangle face");
		}

		// Parse vertices for this face
		for(String face : args) {
			// Tokenize face
			final String[] parts = face.trim().split("/");
			if(parts.length > 3) throw new IllegalArgumentException("Invalid face: " + face);

			// Clean
			for(int n = 0; n < parts.length; ++n) {
				parts[n] = parts[n].trim();
			}

			// Parse mandatory vertex position index
			final int v = Integer.parseInt(parts[0]);

			// Parse optional normal index
			final Integer n = parts.length == 3 ? Integer.parseInt(parts[2]) : null;

			// Parse optional texture coordinate index
			final Integer tc;
			if((parts.length > 1) && !parts[1].isEmpty()) {
				tc = Integer.parseInt(parts[1]);
			}
			else {
				tc = null;
			}

			// Add vertex
			model.vertex(v, n, tc);
		}
	}
}
