package org.sarge.jove.model.obj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Scanner;

import org.sarge.lib.util.MapBuilder;

/**
 * Loader for <tt>OBJ</tt> format models.
 * @author Sarge
 * TODO
 * - break into two *instances* of this map-lookup-delegate
 * - how to deal with fact that material loader/parser needs to know directory?
 */
public class ObjectModelLoader {
	private final Map<String, Parser> materialParsers = registerMaterialParsers().build();
	private final Map<String, Parser> modelParsers = registerModelParsers().build();

	/**
	 * @return Model parsers
	 */
	protected MapBuilder<String, Parser> registerModelParsers() {
		return new MapBuilder<String, Parser>()
				// Vertex data
				.add("v", new VertexParser())
				.add("vt", new TextureCoordinateParser())
				.add("vn", new NormalParser())

				// Polygons
				.add("f", new FaceParser())

				// Object groups
				.add("g", new GroupParser())
				.add("o", new GroupParser())

				// Material library
				.add("usemtl", new UseMaterialParser())
				.add("mtllib", new MaterialLibraryParser(materialParsers));
	}

	/**
	 * @return Material parsers
	 */
	protected MapBuilder<String, Parser> registerMaterialParsers() {
		return new MapBuilder<String, Parser>()
				// Control
				.add("newmtl", new NewMaterialParser())
				
				// Colours
				.add("Ka", new ColourParser("Ka"))
				.add("Kd", new ColourParser("Kd"))
				.add("Ks", new ColourParser("Ks"))

				// Illumination model
				.add("illum", new IlluminationParser())

				// Textures
				.add("map_Kd", new TextureParser("colourMap"));

		//			final TextureLoader textureLoader = new TextureLoader( sys.getImageLoader( src ), sys );
		//			register( "map_Kd", new TextureMaterialParser( "colourMap", textureLoader ) );

		// TODO
		// - Ns			specular coefficient 0..1000
		// - Tr or d	transparency
		// - map_Kd		diffuse (usually same as ambient)
		// - map_Ks		specular
		// - map_Ns		highlight component
		// - map_d		alpha texture
		// - map_bump	bump texture (can be bump)
		// - options
		// http://en.wikipedia.org/wiki/Wavefront_.obj_file
	}

	/**
	 * Loads an OBJ model from the given data-stream.
	 * @param src		Data-source
	 * @param path		OBJ file
	 * @return Model
	 * @throws IOException if the model cannot be loaded
	 */
	public ObjectModel load(Reader in) throws IOException {
		final ObjectModel model = new ObjectModel();
		load(in, modelParsers, model);
		return model;
	}
	
	/**
	 * Helper - Loads and parses an OBJ model or material file.
	 * @param path			File-path
	 * @param parsers		Parsers ordered by command
	 * @param model			Model
	 * @throws IOException if the file cannot be parsed
	 */
	public static <T> void load(Reader in, Map<String, Parser> parsers, ObjectModel model) throws IOException {
		try(final BufferedReader r = new BufferedReader(in)) {
			final Scanner scanner = new Scanner(in);
			while(true) {
				// Stop at EOF
				if(!scanner.hasNext()) {
					break;
				}

				// Lookup parser for next command
				final String cmd = scanner.next();
				final Parser p = parsers.get(cmd);
				if(p == null) throw new IOException("Unknown OBJ command: " + cmd);
				
				// Delegate
				p.parse(scanner, model);
			}
		}
	}
}
