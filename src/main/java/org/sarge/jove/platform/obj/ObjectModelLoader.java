package org.sarge.jove.platform.obj;

import static org.sarge.lib.util.Check.notNull;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.sarge.jove.geometry.*;
import org.sarge.jove.io.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.jove.model.Mesh;
import org.sarge.jove.util.FloatArrayConverter;
import org.sarge.lib.util.Check;

/**
 * Loader for an OBJ model.
 * @see <a href="https://en.wikipedia.org/wiki/Wavefront_.obj_file">OBJ file format</a>
 * @author Sarge
 */
public class ObjectModelLoader extends TextLoader implements ResourceLoader<Reader, List<Mesh>> {
	private final Map<String, Parser> parsers = new HashMap<>();
	private final ObjectModel model = new ObjectModel();
	private Consumer<String> handler = line -> { /* Ignored */ };

	/**
	 * Constructor.
	 */
	public ObjectModelLoader() {
		init();
	}

	/**
	 * Registers default command parsers.
	 */
	private void init() {
		add("v",  new VertexComponentParser<>(new FloatArrayConverter<>(Point.SIZE, Point::new), ObjectModel::positions));
		add("vt", new VertexComponentParser<>(new FloatArrayConverter<>(2, ObjectModelLoader::flip), ObjectModel::coordinates));
		add("vn", new VertexComponentParser<>(new FloatArrayConverter<>(Normal.SIZE, Normal::new), ObjectModel::normals));
		add("f", new FaceParser());
		add("o", Parser.GROUP);
		add("g", Parser.GROUP);
		add("s", Parser.IGNORE);
	}

	/**
	 * Registers a command parser.
	 * @param token			Command token
	 * @param parser		Parser
	 */
	public void add(String token, Parser parser) {
		Check.notEmpty(token);
		Check.notNull(parser);
		parsers.put(token, parser);
	}

	/**
	 * Vertically flips a texture coordinate.
	 */
	private static Coordinate2D flip(float[] array) {
		assert array.length == 2;
		return new Coordinate2D(array[0], -array[1]);
	}

	/**
	 * Sets the callback handler for unknown OBJ commands.
	 * The default handler silently ignores unknown commands.
	 * @param handler Unknown command handler
	 */
	public void setUnknownCommandHandler(Consumer<String> handler) {
		this.handler = notNull(handler);
	}

	@Override
	public Reader map(InputStream in) throws IOException {
		return new InputStreamReader(in);
	}

	@Override
	public List<Mesh> load(Reader reader) throws IOException {
		super.load(reader, this::parse);
		return model.models();
	}

	/**
	 * Parses a line of the model.
	 * @param line Line
	 */
	private void parse(String line) {
		// Tokenize to command and arguments
		final String[] parts = StringUtils.split(line, null, 2);

		// Lookup command parser
		final Parser parser = parsers.get(parts[0]);
		if(parser == null) {
			handler.accept(line);
			return;
		}

		// Delegate
		if(parts.length == 1) {
			parser.parse(null, model);
		}
		else {
			parser.parse(parts[1], model);
		}
	}
}
