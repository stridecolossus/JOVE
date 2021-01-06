package org.sarge.jove.platform.obj;

/**
 * The <i>face parser</i> parses an OBJ face command.
 * <p>
 * TODO - doc format, assumptions, constraints
 * @author Sarge
 */
public class FaceParser implements Parser {
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
