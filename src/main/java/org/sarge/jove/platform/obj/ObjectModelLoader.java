package org.sarge.jove.platform.obj;

import static org.sarge.lib.util.Check.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.sarge.jove.common.Coordinate;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model;
import org.sarge.jove.util.ResourceLoader;
import org.sarge.lib.util.Check;

/**
 * Loader for an OBJ model.
 * @author Sarge
 */
public class ObjectModelLoader implements ResourceLoader<Reader, Stream<Model>> {
	/**
	 * Adapter to parse flipped texture coordinates.
	 */
	private static final Function<float[], Coordinate> FLIP = array -> {
		array[1] = -array[1];
		return Coordinate.of(array);
	};

	private final Map<String, Parser> parsers = new HashMap<>();
	private Set<String> comments = Set.of("#");
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
		add("vt", new VertexComponentParser<>(2, FLIP, ObjectModel::coordinate));
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
	 * Sets the comment tokens.
	 * @param comments Comment tokens
	 */
	public void setCommentTokens(Set<String> comments) {
		this.comments = Set.copyOf(comments);
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

	/**
	 * Loads an OBJ model.
	 * @param r Reader
	 * @return Resultant model(s)
	 * @throws IOException if the model cannot be loaded
	 * @see #create()
	 */
	@Override
	public Stream<Model> load(Reader r) throws IOException {
		// Parse OBJ model
		try(final LineNumberReader in = new LineNumberReader(r)) {
			try {
				in.lines()
					.map(String::trim)
					.filter(Predicate.not(String::isBlank))
					.filter(Predicate.not(this::isComment))
					.forEach(this::parse);
			}
			catch(Exception e) {
				throw new IOException(String.format("%s at line %d", e.getMessage(), in.getLineNumber()), e);
			}
		}

		// Construct models
		return model.build();
	}

	/**
	 * Tests whether the given line is a comment.
	 */
	private boolean isComment(String line) {
		return comments.stream().anyMatch(line::startsWith);
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
