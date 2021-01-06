package org.sarge.jove.platform.obj;

import static org.sarge.jove.util.Check.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate2D;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Model.IndexedBuilder;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.util.Check;
import org.sarge.jove.util.Loader.LoaderAdapter;

/**
 * Loader for an OBJ model.
 * @author Sarge
 */
public class ObjectModelLoader extends LoaderAdapter<Reader, Stream<Model>> {
	private static final String[] EMPTY_ARGUMENTS = new String[]{};

	/**
	 * Handler to ignore unknown commands.
	 */
	public static final Consumer<String> HANDLER_IGNORE = str -> {
		// Empty block
	};

	/**
	 * Handler that throws an exception.
	 * @throws IllegalArgumentException for an unknown command
	 */
	public static final Consumer<String> HANDLER_THROW = str -> {
		throw new IllegalArgumentException("Unsupported OBJ command: " + str);
	};

	private final Map<String, Parser> parsers = new HashMap<>();
	private Set<String> comments = Set.of("#");
	private Consumer<String> handler = HANDLER_THROW;

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
		add("v", new ArrayParser<>(Point.SIZE, Point::new, ObjectModel::vertices));
		add("vt", new ArrayParser<>(Coordinate2D.SIZE, Coordinate2D::new, ObjectModel::coordinates));
		add("vn", new ArrayParser<>(Vector.SIZE, Vector::new, ObjectModel::normals));
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

	/**
	 * Creates a new transient OBJ model.
	 * <p>
	 * The default implementation creates an {@link IndexedBuilder} with the following configuration:
	 * <ul>
	 * <li>a {@link Primitive#TRIANGLES} drawing primitive</li>
	 * <li>vertex indices automatically added (see {@link IndexedBuilder#setAutoIndex(boolean)})</li>
	 * </ul>
	 * <p>
	 * Over-ride to provide a custom OBJ model and/or underlying builder.
	 * <p>
	 * @return New OBJ model
	 */
	protected ObjectModel create() {
		final Supplier<Model.Builder> factory = () -> new Model.IndexedBuilder().setAutoIndex(true).primitive(Primitive.TRIANGLES);
		return new ObjectModel(factory);
	}

	@Override
	protected Reader open(InputStream in) {
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
		// Create transient model
		final ObjectModel model = create();

		// Parse OBJ model
		try(final LineNumberReader in = new LineNumberReader(r)) {
			try {
				in.lines()
					.map(String::trim)
					.filter(Predicate.not(String::isBlank))
					.filter(Predicate.not(this::isComment))
					.forEach(line -> parse(line, model));
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
	private void parse(String line, ObjectModel model) {
		// Tokenize line
		final String[] parts = StringUtils.split(line, null, 2);

		// Lookup command parser
		final Parser parser = parsers.get(parts[0].trim());
		if(parser == null) {
			handler.accept(line);
			return;
		}

		// Delegate
		if(parts.length == 1) {
			parser.parse(EMPTY_ARGUMENTS, model);
		}
		else {
			final String[] args = StringUtils.split(parts[1]);
			parser.parse(args, model);
		}
	}
}
