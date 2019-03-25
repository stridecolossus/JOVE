package org.sarge.jove.obj;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.lang3.StringUtils;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.obj.ObjectMaterial.Library;
import org.sarge.jove.obj.ObjectModel.Face;
import org.sarge.jove.texture.TextureCoordinate;

/**
 * Loader for an OBJ model.
 * @author Sarge
 */
public class ObjectModelLoader extends DefaultObjectLoader<ObjectModel> {
	/**
	 * Group/object parser.
	 */
	static final Parser<ObjectModel> GROUP = (tokens, model) -> model.group(tokens[1]);

	// TODO
	// - automatically build index buffer for duplicate vertices

	/**
	 * Face parser.
	 */
	static class FaceParser implements Parser<ObjectModel> {
		@Override
		public void parse(String[] tokens, ObjectModel model) {
			for(int n = 1; n < tokens.length; ++n) {
				// Tokenize face indices
				final String[] parts = StringUtils.splitPreserveAllTokens(tokens[n], Face.DELIMITER);
				if(parts.length > 3) throw new IllegalArgumentException(String.format("Invalid number of face components: [%s]", tokens[n]));

				// Parse vertex index
				final int vertex = Integer.parseInt(parts[0]);

				// Parse optional texture coordinate index
				int coords = 0;
				if(parts.length > 1) {
					if(parts[1].length() > 0) {
						coords = Integer.parseInt(parts[1]);
					}
				}

				// Parse normal index
				int normal = 0;
				if(parts.length == 3) {
					normal = Integer.parseInt(parts[2]);
				}

				// Create face and add to model
				final Face face = new Face(vertex, coords, normal);
				model.group().face(face);
			}
		}

		@Override
		public int min() {
			return 3;
		}
	}

	/**
	 * Parser for an OBJ material library.
	 */
	class MaterialLibraryParser implements Parser<ObjectModel> {
		private final MaterialLoader loader = new MaterialLoader();

		@Override
		public void parse(String[] tokens, ObjectModel model) {





			// Open material library
			final String filename = tokens[1];



			// TODO - open file
			// TODO - loop

			// Parse material name
			// TODO - parse newmtl token -> name
			final String name = "name";

			// Load material
			final ObjectMaterial.Builder builder = new ObjectMaterial.Builder(name);
			//loader.load(r, builder);
			// TODO - load

			// Add to library
			final ObjectMaterial mat = builder.build();
			lib.add(mat);
		}
	}

	/**
	 * Select material parser.
	 */
	class MaterialParser implements Parser<ObjectModel> {
		@Override
		public void parse(String[] tokens, ObjectModel model) {
			final ObjectMaterial mat = lib.find(tokens[1]);
			model.group().material(mat);
		}
	}

	private final Library lib = new Library();

	/**
	 * Constructor.
	 */
	public ObjectModelLoader() {
		// Vertex parsers
		add("v", new ArrayParser<>(Point.SIZE, Point::new, (v, model) -> model.group().vertex(v)));
		add("vn", new ArrayParser<>(Vector.SIZE, Vector::new, (n, model) -> model.group().normal(n)));
		add("vt", new ArrayParser<>(2, TextureCoordinate.Coordinate2D::new, (tc, model) -> model.group().coords(tc)));

		// Object/group parser
		add("g", GROUP);
		add("o", GROUP);

		// Face parser
		add("f", new FaceParser());

		// Material parsers
		add("mtllib", new MaterialLibraryParser());
		add("usemtl", new MaterialParser());
	}

	/**
	 * @return Material library
	 */
	public Library library() {
		return lib;
	}

	/**
	 * Loads an OBJ model.
	 * @param r Reader
	 * @return Model
	 * @throws IOException if the model cannot be loaded
	 */
	public ObjectModel load(Reader r) throws IOException {
		final ObjectModel model = new ObjectModel();
		load(r, model);
		return model;
	}
}
