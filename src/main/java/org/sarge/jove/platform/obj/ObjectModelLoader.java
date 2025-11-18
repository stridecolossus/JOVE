package org.sarge.jove.platform.obj;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireNotEmpty;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.jove.model.IndexedMesh;

/**
 * Loader for an OBJ model.
 * @see <a href="https://en.wikipedia.org/wiki/Wavefront_.obj_file">OBJ file format</a>
 * @author Sarge
 */
public class ObjectModelLoader {
	private final Map<String, Parser> parsers = new HashMap<>();
	private final ObjectModel model = new ObjectModel();
	private Consumer<String> handler = _ -> { /* Ignored */ };

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
		final var group = new GroupParser(model);
		add("o", group);
		add("g", group);
		add("s", Parser.IGNORE);
		add("v",  new VertexComponentParser<>(Point.SIZE, Point::new, model.positions()));
		add("vt", new VertexComponentParser<>(2, ObjectModelLoader::flip, model.coordinates()));
		add("vn", new VertexComponentParser<>(Normal.SIZE, ObjectModelLoader::normal, model.normals()));
		add("f", new FaceParser(model));
	}

	/**
	 * Registers a command parser.
	 * @param token			Command token
	 * @param parser		Parser
	 */
	public void add(String token, Parser parser) {
		requireNotEmpty(token);
		requireNonNull(parser);
		parsers.put(token, parser);
	}

	/**
	 * Vertically flips a texture coordinate.
	 */
	private static Coordinate2D flip(float[] array) {
		return new Coordinate2D(array[0], -array[1]);
	}

	/**
	 * Array constructor adapter for a normal.
	 */
	private static Normal normal(float[] array) {
		return new Normal(new Vector(array));
	}

	/**
	 * Sets the callback handler for unknown OBJ commands.
	 * By default unknown commands are silently ignored.
	 * @param handler Unknown command handler
	 */
	public void setUnknownCommandHandler(Consumer<String> handler) {
		this.handler = requireNonNull(handler);
	}

	// TODO - Path.of(ClassLoader.getSystemResource(resourceName).toURI());

	/**
	 * Loads an OBJ model from the given source.
	 * @param input Input
	 * @return OBJ model(s)
	 * @throws IOException if the model cannot be loaded
	 */
	public List<IndexedMesh> load(Reader input) throws IOException {
		// Open file
		final var reader = new LineNumberReader(input);

		// Parse commands
		try(reader) {
			reader
    				.lines()
    				.map(ObjectModelLoader::clean)
    				.map(String::trim)
    				.filter(Predicate.not(String::isEmpty))
    				.forEach(this::parse);
		}
		catch(RuntimeException e) {
			throw new IOException(String.format("%s at %d", e.getMessage(), reader.getLineNumber()), e);
		}

		// Build model
		return model.build();
	}

	/**
	 * Removes comments.
	 */
	private static String clean(String line) {
		final int index = line.indexOf('#');
		if(index > 0) {
			return line.substring(0, index);
		}
		else {
			return line;
		}
	}

	/**
	 * Parses a line of the model.
	 * @param line Line
	 */
	private void parse(String line) {
		try(final var scanner = new Scanner(line)) {
			// Find parser for this command
			final Parser parser = parsers.get(scanner.next());

			// Notify unknown commands
    		if(parser == null) {
    			handler.accept(line);
    			return;
    		}

    		// Delegate
    		parser.parse(scanner);
		}
	}
}
