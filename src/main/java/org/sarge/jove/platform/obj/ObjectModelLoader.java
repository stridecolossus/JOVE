package org.sarge.jove.platform.obj;

import static org.sarge.lib.util.Check.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.io.ResourceLoader;
import org.sarge.jove.io.TextLoader;
import org.sarge.jove.model.Model;
import org.sarge.lib.util.Check;

/**
 * Loader for an OBJ model.
 * @author Sarge
 */
public class ObjectModelLoader implements ResourceLoader<Reader, List<Model>> {
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
		add("v",  new VertexComponentParser<>(Point.SIZE, Point::new, ObjectModel::position));
		add("vt", new VertexComponentParser<>(2, ObjectModelLoader::flip, ObjectModel::coordinate));
		add("vn", new VertexComponentParser<>(Vector.SIZE, Vector::new, ObjectModel::normal));
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
	 * Sets the callback handler for unknown commands (default is {@link #HANDLER_THROW}).
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
	public List<Model> load(Reader reader) throws IOException {
		final Function<Stream<String>, List<Model>> mapper = lines -> {
			lines.forEach(this::parse);
			return model.build();
		};
		final TextLoader loader = new TextLoader();
		return loader.load(reader, mapper);
	}

	/**
	 * Parses a line of the model.
	 * @param line Line
	 * @throws IllegalArgumentException if the command is unsupported
	 */
	private void parse(String line) {
		// Tokenize line
		final String[] parts = StringUtils.split(line);
		Parser.trim(parts);

		// Lookup command parser
		final Parser parser = parsers.get(parts[0]);
		if(parser == null) {
			handler.accept(line);
			return;
		}

		// Delegate
		parser.parse(parts, model);
	}
}
