package org.sarge.jove.platform.obj;

import static org.sarge.jove.util.Check.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate2D;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Model.IndexedBuilder;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.util.Check;
import org.sarge.jove.util.Loader.LoaderAdapter;

/**
 * Loader for an OBJ model.
 * @author Sarge
 */
public class ObjectModelLoader extends LoaderAdapter<Reader, Model> {
	/**
	 * The <i>object model</i> holds the transient OBJ data during parsing.
	 */
	public static class ObjectModel {
		private final List<Point> vertices = new ArrayList<>();
		private final List<Coordinate2D> coords = new ArrayList<>();
		private final List<Vector> normals = new ArrayList<>();
		private final Model.Builder builder;
		private boolean flip = true;
		private boolean init;

		/**
		 * Constructor.
		 * @param Underlying model builder
		 */
		protected ObjectModel(Model.Builder builder) {
			this.builder = notNull(builder);
		}

		/**
		 * Sets whether to vertically flip texture coordinates (default is {@code true}).
		 * @param flip Whether to vertically flip coordinates
		 */
		public void flip(boolean flip) {
			this.flip = flip;
		}

		/**
		 * Adds a vertex position.
		 * @param pos Vertex position
		 */
		protected void vertex(Point pos) {
			vertices.add(notNull(pos));
		}

		/**
		 * Adds a vertex normal.
		 * @param normal Normal
		 */
		protected void normal(Vector normal) {
			normals.add(notNull(normal));
		}

		/**
		 * Adds a vertex texture coordinate.
		 * @param coords Texture coordinate
		 * @see #flip(boolean)
		 */
		protected void coord(Coordinate2D coords) {
			if(flip) {
				this.coords.add(new Coordinate2D(coords.u, -coords.v));
			}
			else {
				this.coords.add(coords);
			}
		}

		/**
		 * Adds a vertex to the model builder.
		 * @param vertex Vertex
		 * @see Model.Builder#add(Vertex)
		 */
		protected void add(Vertex vertex) {
			builder.add(vertex);
		}

		/**
		 * Updates or validates the primitive for the given face size.
		 * @param size Face size
		 */
		void update(int size) {
			if(init) {
				// Check face matches existing primitive
				if(size != builder.primitive().size()) {
					throw new IllegalArgumentException(String.format("Face size mismatch: expected=%d actual=%d", builder.primitive().size(), size));
				}
			}
			else {
				// Initialise model
				init(size);
				init = true;
			}
		}

		/**
		 * Initialises this model.
		 */
		private void init(int size) {
			// Init primitive
			final Primitive primitive = switch(size) {
				case 1 -> Primitive.POINTS;
				case 2 -> Primitive.LINES;
				case 3 -> Primitive.TRIANGLES;
				default -> throw new UnsupportedOperationException("Unsupported primitive size: " + size);
			};
			builder.primitive(primitive);

			// Init layout
			final var layout = new ArrayList<Vertex.Component>();
			layout.add(Vertex.Component.POSITION);
			if(!normals.isEmpty()) {
				layout.add(Vertex.Component.NORMAL);
			}
			if(!coords.isEmpty()) {
				layout.add(Vertex.Component.TEXTURE_COORDINATE);
			}
			builder.layout(new Vertex.Layout(layout));
		}

		/**
		 * Constructs the final model.
		 * @return New model
		 */
		Model build() {
			return builder.build();
		}
	}

	/**
	 * Parser for a OBJ command line.
	 */
	public interface Parser {
		/**
		 * Parses the given arguments.
		 * @param args Arguments
		 * @throws NumberFormatException is the data cannot be parsed
		 */
		void parse(String[] args, ObjectModel model);

		/**
		 * Parser that ignores the arguments.
		 */
		Parser IGNORE = (args, model) -> {
			// Does nowt
		};

		/**
		 * Creates a parser for a floating-point array command.
		 * @param <T> Data-type
		 * @param size			Expected size of the data
		 * @param ctor			Array constructor
		 * @param setter		Model setter method
		 * @return New array parser
		 */
		static <T> Parser of(int size, Function<float[], T> ctor, BiConsumer<ObjectModel, T> setter) {
			return (args, model) -> {
				// Validate
				if(args.length != size) {
					throw new IllegalArgumentException(String.format("Invalid number of tokens: expected=%d actual=%d", size, args.length));
				}

				// Convert to array
				final float[] array = new float[size];
				for(int n = 0; n < size; ++n) {
					array[n] = Float.parseFloat(args[n].trim());
				}

				// Create object using array constructor
				final T value = ctor.apply(array);

				// Add to transient model
				setter.accept(model, value);
			};
		}

		/**
		 * Face parser.
		 */
		Parser FACE = new Parser() {
			@Override
			public void parse(String[] args, ObjectModel model) {
				// Update/validate face size
				model.update(args.length);

				// Parse face and add vertices
				for(String face : args) {
					// Tokenize face
					final String[] parts = face.trim().split("/");
					if(parts.length > 3) throw new IllegalArgumentException("Invalid face: " + face);

					// Lookup vertex position
					final Vertex.Builder vertex = new Vertex.Builder();
					final Point pos = lookup(model.vertices, parts[0].trim());
					vertex.position(pos);

					// Lookup optional texture coordinate
					if(parts.length > 1) {
						final Coordinate2D coords = lookup(model.coords, parts[1].trim());
						vertex.coords(coords);
					}

					// Lookup vertex normal
					if(parts.length == 3) {
						final Vector normal = lookup(model.normals, parts[2].trim());
						vertex.normal(normal);
					}

					// Add vertex
					model.add(vertex.build());
				}
			}

			/**
			 * Helper - Retrieves an element from the given list.
			 * @param <T> List type
			 * @param list		List
			 * @param str		Index string (1..n, can be negative)
			 * @return Specified element
			 * @throws IndexOutOfBoundsException if the index is zero is out-of-bounds for the given list
			 * @throws NumberFormatException if the index string cannot be parsed
			 */
			private <T> T lookup(List<T> list, String str) {
				final int index = Integer.parseInt(str.trim());
				if(index > 0) {
					return list.get(index - 1);
				}
				else
				if(index < 0) {
					return list.get(list.size() + index);
				}
				else {
					throw new IndexOutOfBoundsException("Invalid zero index");
				}
			}
		};
	}

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
		add("v", Parser.of(Point.SIZE, Point::new, ObjectModel::vertex));
		add("vt", Parser.of(Coordinate2D.SIZE, Coordinate2D::new, ObjectModel::coord));
		add("vn", Parser.of(Vector.SIZE, Vector::new, ObjectModel::normal));
		add("f", Parser.FACE);
		add("s", Parser.IGNORE);
		add("g", Parser.IGNORE);
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
	 * Over-ride to implement a custom OBJ model.
	 * @return New OBJ model
	 * @see #builder()
	 */
	protected ObjectModel model() {
		return new ObjectModel(builder());
	}

	/**
	 * Creates the underlying model builder.
	 * <p>
	 * The default implementation creates an {@link IndexedBuilder} with the following configuration:
	 * <ul>
	 * <li>a {@link Primitive#TRIANGLES} drawing primitive</li>
	 * <li>vertex indices automatically added (see {@link IndexedBuilder#setAutoIndex(boolean)})</li>
	 * </ul>
	 * <p>
	 * Over-ride to modify the type or properties of the underlying builder.
	 * <p>
	 * @return New model builder
	 */
	protected Model.Builder builder() {
		return new Model.IndexedBuilder()
				.setAutoIndex(true)
				.primitive(Primitive.TRIANGLES);
	}

	@Override
	protected Reader open(InputStream in) {
		return new InputStreamReader(in);
	}

	/**
	 * Loads an OBJ model.
	 * @param r Reader
	 * @return Resultant model builder
	 * @throws IOException if the model cannot be loaded
	 * @see #model()
	 */
	@Override
	public Model load(Reader r) throws IOException {
		// Create transient model
		final ObjectModel model = model();

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

		// Construct model
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
