# OBJ Model Loader

## File Format

An OBJ model is a text-based data format consisting of a series of *commands* to define a static model.
Each line starts with a *command token* followed by a number of space-delimited arguments.

The most common commands are:

| command     | arguments     | purpose                 | example             |
| -------   | ---------        | -------                | -------            |
| v           | x y x              | vertex position        | v 0.1 0.2 0.3     |
| vn        | x y z            | normal                | vn 0.4 0.5 0.6    |
| vt        | u v            | texture coordinate    | vt 0.7 0.8        |
| f            | see below        | face or triangle        | f 1//3 4//6 7//9    |
| s            | n/a            | smoothing group        | s                    |

The *face* command specifies the vertices of a polygon (usually a triangle) as a tuple of indices delimited by the slash character.
Each vertex consists of a position index, optional normal index, and an optional texture coordinate index.

| example                 | description                                 |
| -------               | -----------                               |
| f 1 2 3                | triangle with vertex positions only         |
| f 1/2 3/4 5/6            | triangle also with texture coordinates     |
| f 1/2/3 4/5/6 7/8/9    | also with normals                         |
| f 1//2 3//4 5//6        | normals but no texture coordinates         |

Example:

```
v 0.1 0.2 0.3
v 0.4 0.5 0.6
v 0.7 0.8 0.9
vt 0 0
vt 0 1
vt 1 0
f 1/1 2/2 3/3
```

## Scope

For this chapter is to create an OBJ model the requires the minimal functionality outlined above.

Therefore we have the following constraints on the scope:

- Face primitives are assumed to be triangles.

- The examples we using to test against all have a single smoothing group - at some point we will need to extend the scope to generate multiple a model-per-group.

- The OBJ format supports material descriptors that specify texture properties (amongst others) - for the moment we will hard-code the associated texture image name.

## Model Loader

### File Parser

We start with a loader for an OBJ model.

The loading process is:
1. Create a transient model for the OBJ object (detailed below).
2. Load each line of the OBJ model file, skipping empty lines and comments.
3. Parse each command line.
4. Create the resultant model.

```java
public class ObjectModelLoader {
    public Model load(Reader r) throws IOException {
        // Create transient model
        final ObjectModel model = model();

        // Parse OBJ model
        try(final LineNumberReader in = new LineNumberReader(r)) {
            in.lines()
                .map(String::trim)
                .filter(Predicate.not(String::isBlank))
                .filter(Predicate.not(this::isComment))
                .forEach(line -> parse(line, model));
        }
        catch(Exception e) {
            throw new IOException(String.format("%s at line %d", e.getMessage(), in.getLineNumber()), e);
        }

        // Construct model
        return model.build();
    }
}
```

The `isComment` method is as follows:

```java
private boolean isComment(String line) {
    return comments.stream().anyMatch(line::startsWith);
}
```

and we add a setter that allows the user to specify the comment token(s).

### Command Parsers

The `parse()` method first separates the command token from the arguments and then delegates to a `Parser` for that command:

```java
private void parse(String line, ObjectModel model) {
    // Tokenize line
    final String[] parts = StringUtils.split(line, null, 2);

    // Lookup command parser
    final Parser parser = parsers.get(parts[0]);
    if(parser == null) ...

    // Delegate
    final String[] args = StringUtils.split(parts[1]);
    parser.parse(args, model);
}
```

A `Parser` is defined as follows:

```java
public interface Parser {
    /**
     * Parses the given arguments.
     * @param args         Arguments
     * @param model        OBJ model
     * @throws NumberFormatException is the data cannot be parsed
     */
    void parse(String[] args, ObjectModel model);

    /**
     * Parser that ignores the arguments.
     */
    Parser IGNORE = (args, model) -> {
        // Does nowt
    };
}
```

The loader registers the most common command parsers in its constructor:

```java
public ObjectModelLoader() {
    init();
}

private void init() {
    add("v", Parser.of(Point.SIZE, Point::new, ObjectModel::vertex));
    add("vt", Parser.of(Coordinate2D.SIZE, Coordinate2D::new, ObjectModel::coord));
    add("vn", Parser.of(Vector.SIZE, Vector::new, ObjectModel::normal));
    add("f", Parser.FACE);
    add("s", Parser.IGNORE);
    add("g", Parser.IGNORE);
}
```

The `add` method is public and can be used to register or over-ride a command parser.

There are two built-in command parser implementations detailed below.

### OBJ Transient Model

The `ObjectModel` is a transient working representation of the OBJ model (vertices, normals, texture coordinates) which is later transformed into a JOVE model:

```java
/**
 * The <i>object model</i> holds the transient OBJ data during parsing.
 */
public static class ObjectModel {
    private final List<Point> vertices = new ArrayList<>();
    private final List<Coordinate2D> coords = new ArrayList<>();
    private final List<Vector> normals = new ArrayList<>();
    private final Model.Builder builder;

    /**
     * Constructor.
     * @param Underlying model builder
     */
    protected ObjectModel(Model.Builder builder) {
        this.builder = notNull(builder);
    }
}
```

Note that the transient model contains an instance of a JOVE model builder which is created by `builder` and can also be over-ridden as required.

The transient model itself is created by the protected `model` method which can be over-ridden to alter or extend the implementation.

### Error Handling

The OBJ format is quite vague and models are notoriously flakey so our loader needs to be robust but not overly stringent.

The `add` method is used to register a command parser and unsupported or unknown commands can be skipped by the `IGNORE` parser.

We also add a callback handler with implementations to either ignore unknown commands or throw an exception depending on the application:

```java
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

private Consumer<String> handler = HANDLER_THROW;

/**
 * Sets the callback handler for unknown commands (default is {@link #HANDLER_THROW}).
 * @param handler Unknown command handler
 */
public void setUnknownCommandHandler(Consumer<String> handler) {
    this.handler = notNull(handler);
}

private void parse(String line, ObjectModel model) {
    ...
    if(parser == null) {
        handler.accept(line);
        return;
    }
    ...
}
```

## Vertex Components

Command parsers for the vertex components (position, normal, texture coordinate) are created via a factory method:

```java
/**
 * Creates a parser for a floating-point array command.
 * @param <T> Data-type
 * @param size            Expected size of the data
 * @param ctor            Array constructor
 * @param setter        Model setter method
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
            array[n] = Float.parseFloat(args[n]);
        }

        // Create object using array constructor
        final T value = ctor.apply(array);

        // Add to transient model
        setter.accept(model, value);
    };
}
```

This parser performs the following:
1. Converts the arguments to a floating-point array
2. Constructs a vertex component from this array
3. Add the result to the transient model

For example, the parser for the vertex position command is declared thus:

```java
Parser.of(Point.SIZE, Point::new, ObjectModel::vertex);
```

We add array constructors to the relevant domain classes.

## Faces

### Face Parser

The `FACE` parser iterates over the vertices of a face and adds a `Vertex` instance to the builder:

```java
for(String face : args) {
    // Tokenize face
    final String[] parts = face.trim().split("/");

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
```

The `lookup` helper parses an index and retrieves the specified vertex component from the transient model.
Note that face indices can also be negative specifying a reverse index from the end of a list.

### Model Initialisation

One of the issues we face (pun intended) when parsing an OBJ model is that we do not know the vertex layout or drawing primitive up-front.
We could mandate that the developer is responsible for configuring the model before loading but this feels overly restrictive - 
it would be much nicer if the loader determined this information for us.
Therefore the face parser *initialises* the model when we first encounter a face definition.

The face parser invokes the following method on the transient model for every face:

```java
void update(int size) {
    if(init) {
        // Check face matches existing primitive
        if(size != builder.primitive().size()) throw new IllegalArgumentException(...);
    }
    else {
        // Initialise model
        init(size);
        init = true;
    }
}
```

The _init_ flag is used to determine whether the model has been initialised, each subsequent face is validated against the selected primitive.

The init() method determines the vertex layout and primitive based on the number of vertices in the face:

```java
private void init(int size) {
    // Init primitive
    final Primitive primitive = switch(size) {
        case 1 -> Primitive.POINTS;
        case 2 -> Primitive.LINES;
        case 3 -> Primitive.TRIANGLES;
        default -> throw new IllegalArgumentException("Unsupported primitive size: " + size);
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
```

---

# Indexed Models

We know that the OBJ model that we will be using in our demo contains a large number of duplicate vertices which we have ignored until now.
The obvious next step is remove the duplicates and introduce an *index buffer* to the model building process.
This will reduce the total amount of data in the vertex buffer (at the expense of a second buffer for the index).

## Vertex De-Duplication

We implement the following model builder sub-class that maintains a map of the vertex indices so we can test for duplicates:

```java
public static class IndexedBuilder extends Builder {
    private final List<Integer> index = new ArrayList<>();
    private final Map<Vertex, Integer> map = new HashMap<>();

    private boolean auto = true;

    /**
     * Sets whether to automatically add an index for each vertex (default is {@code true}).
     * @param auto Whether to automatically add indices
     */
    public IndexedBuilder setAutoIndex(boolean auto) {
        this.auto = auto;
        return this;
    }
}
```

The add() method in the indexed builder intercepts duplicate vertices, a new vertex is added to the model as normal and its index is registered in the map:

```java
public Builder add(Vertex vertex) {
    // Lookup existing vertex index
    final Integer prev = map.get(vertex);

    if(prev == null) {
        // Add new vertex
        map.put(vertex, count());
        super.add(vertex);
    }
}
```

The _auto_ setting is used to automatically add an index for each vertex (including duplicates) which is used in the OBJ loader:

```java
if(prev == null) {
    ...
    
    // Add index for new vertex
    if(auto) {
        add(map.size() - 1);
    }
}
else {
    // Add index for existing vertex
    if(auto) {
        add(prev);
    }
}
```

We also add new methods to explicitly add or lookup indices:

```java
@Override
public Builder add(int index) {
    Check.zeroOrMore(index);
    if(index >= count()) throw new IndexOutOfBoundsException(...);
    this.index.add(index);
    return this;
}

@Override
public int indexOf(Vertex vertex) {
    final Integer index = map.get(vertex);
    if(index == null) throw new IllegalArgumentException(...);
    return index;
}
```

In the base-class model these methods throw an exception.

We can now add an optional index to the model class:

```java
private final List<Integer> index;
private ByteBuffer indexBuffer;

...

@Override
public Optional<ByteBuffer> index() {
    // Check whether indexed
    if(index == null) {
        return Optional.empty();
    }

    // Build IBO
    if(indexBuffer == null) {
        final int[] array = index.stream().mapToInt(Integer::intValue).toArray();
        indexBuffer = Bufferable.allocate(array.length * Integer.BYTES);
        indexBuffer.asIntBuffer().put(array);
    }

    // Prepare index
    return Optional.of(indexBuffer.rewind());
}
```

Finally we refactor the OBJ loader to use the new indexed builder and we also we add the `setAutoIndex()` option to the indexed builder to automatically add an index for each vertex.

All this work reduces the size of interleaved model from 30Mb to roughly 11Mb (5Mb for the vertex data and 6Mb for the index buffer).

Result.

## Model Persistence

When we debug and test the new loader we find that it is now quite slow (despite the fact we are developing on a gaming machine with a decent processor and fast SSD).

One obvious observation is that much of the loading process is repeated *every* time we execute the demo:
1. Load and parse the OBJ file to the transient model.
2. Build the de-duplicated model from the transient model.
3. Generate the interleaved vertex data.
4. Construct the vertex and index buffers.

We really only need to do the above *once* therefore we implement a persistence mechanism for a model.

### Writing a Model

We create the new `ModelLoader` class that will be responsible for reading and writing a buffered model:

```java
public static class ModelLoader {
    /**
     * Writes the given model to an output stream.
     * @param model      Model
     * @param out        Output stream
     * @throws IOException if the model cannot be written
     */
    public void write(Model model, DataOutputStream out) throws IOException {
    }
}
```

We use a `DataOutputStream` to write Java primitives (and more importantly byte arrays) for the various members of a model.

The model primitive is output as a UTF string:

```java
out.writeUTF(model.primitive().name());
```

The layout is a concatenated string representation of the vertex components:

```java
private static final String DELIMITER = "-";

...

final String layout = model.layout().components().stream().map(Enum::name).collect(joining(DELIMITER));
out.writeUTF(layout);
```

Finally we write the vertex and index buffers:

```java
// Write vertex count
out.writeInt(model.count());

// Write VBO
write(model.vertices(), out);

// Write index
final var index = model.index();
if(index.isPresent()) {
    write(index.get(), out);
}
else {
    out.writeInt(0);
}
```

The handling of the optional index buffer is a bit ugly but things get messy when we mix optional values, lambdas and checked exceptions so it will have to do.

The `write` helper outputs the length of a byte buffer and then writes it as an array:

```java
private static void write(ByteBuffer bb, DataOutputStream out) throws IOException {
    final byte[] bytes = new byte[bb.limit()];
    bb.get(bytes);
    out.writeInt(bytes.length);
    out.write(bytes);
}
```

### Reading a Model

We obviously do not want to recreate an instance of the existing model implementation when we load a persisted model so we create a new *buffered model* with the vertex and index buffers as members.  We convert the existing model class to an interface and add a skeleton implementation shared by both:

```java
public class BufferedModel extends AbstractModel {
    private final ByteBuffer vertices;
    private final Optional<ByteBuffer> index;
    private final int count;

    protected BufferedModel(Primitive primitive, Vertex.Layout layout, ByteBuffer vertices, ByteBuffer index, int count) {
        super(primitive, layout);
        this.vertices = notNull(vertices);
        this.index = Optional.ofNullable(index);
        this.count = zeroOrMore(count);
        validate();
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public ByteBuffer vertices() {
        return vertices.rewind();
    }

    @Override
    public Optional<ByteBuffer> index() {
        return index.map(ByteBuffer::rewind);
    }

    public static class ModelLoader {
    }
}
```

The method to read a persisted model uses a `DataInputStream` which is the reverse analogue of the `DataOutputStream` used above:

```java
public Model load(DataInputStream in) throws IOException {
}
```

We introduce a version number to our custom format to check for file compatibility:

```java
private static final int VERSION = 1;

...

// Load and verify file format version
final int version = in.readInt();
if(version > VERSION) {
    throw new UnsupportedOperationException(...);
}
```

and add corresponding code to output the version number in the write method.

The model is loaded and created as follows:

```java
// Load primitive
final Primitive primitive = Primitive.valueOf(in.readUTF());

// Load layout
final var layout = Arrays.stream(in.readUTF().split(DELIMITER)).map(Vertex.Component::valueOf).collect(toList());

// Load vertex count
final int count = in.readInt();

// Load buffers
final ByteBuffer vertices = loadBuffer(in);
final ByteBuffer index = loadBuffer(in);

// Create model
return new BufferedModel(primitive, new Vertex.Layout(layout), vertices, index, count);
```

The helper to load the buffers is slightly more complicated as we also need to handle the case of an empty index buffer:

```java
private static ByteBuffer loadBuffer(DataInputStream in) throws IOException {
    // Read buffer size
    final int len = in.readInt();
    if(len == 0) {
        return null;
    }

    // Load bytes
    final byte[] bytes = new byte[len];
    final int actual = in.read(bytes);
    if(actual != len) throw new IOException(String.format("Error loading buffer: expected=%d actual=%d", len, actual));

    // Convert to buffer
    return Bufferable.allocate(bytes);
}
```

### Conclusion

There is still a lot of conversions of byte buffers to/from arrays but our model can now be loaded in a matter of milliseconds - Nice!

Initially we tried to protect the buffers using _asReadOnlyBuffer()_ but this seemed to break our unit-tests (equality of buffers is complex)
and caused issues when we came to integration so they are exposed as mutable for the moment.

We can now move onto integrating the OBJ model into the demo and see what it looks like.

---

# Loader Support

During the creation of the OBJ and buffered model loaders we took a detour to refactor the various loaders we had implemented so far (including images and shaders).

First we created a _loader_ abstraction:

```java
/**
 * A <i>loader</i> defines a mechanism for loading a resource.
 * @param <T> Input type
 * @param <R> Resource type
 * @author Sarge
 */
@FunctionalInterface
public interface Loader<T, R> {
    /**
     * Loads a resource.
     * @param in Input data
     * @return Loaded resource
     * @throws IOException if the resource cannot be loaded
     */
    R load(T in) throws IOException;
}
```

And defined a base-class adapter that is required to implement the open() method to map an arbitrary resource from an input stream:

```java
/**
 * Adapter for a loader with an intermediate data type mapped from an {@link InputStream}.
 * @param <T> Intermediate type
 * @param <R> Resource type
 */
abstract class LoaderAdapter<T, R> implements Loader<T, R> {
    /**
     * Maps the given input-stream to an instance of the intermediate type.
     * @param in Input-stream
     * @return Intermediate object
     * @throws IOException if the stream cannot be opened
     */
    protected abstract T open(InputStream in) throws IOException;
}
```

Next we added a _data source_ that maps a filename to an input-stream:

```java
public interface DataSource {
    /**
     * Opens the resource with the given name.
     * @param name Resource name
     * @return Input-stream
     * @throws IOException if the resource cannot be opened
     */
    InputStream open(String name) throws IOException;
}
```

and provided an implementation for the a file-system directory:

```java
/**
 * Creates a file-system data-source at the given directory.
 * @param dir Directory
 * @return Data-source
 * @throws IllegalArgumentException if the directory does not exist
 */
static DataSource of(Path dir) {
    if(!Files.exists(dir)) throw new IllegalArgumentException("Data-source directory does not exist: " + dir);
    return name -> Files.newInputStream(dir.resolve(name));
}

/**
 * Creates a file-system data-source at the given directory.
 * @param dir Directory
 * @return Data-source
 * @throws IllegalArgumentException if the directory does not exist
 */
static DataSource of(String dir) {
    return of(Paths.get(dir));
}
```

The final piece of the jigsaw is the following factory method that allows us to compose these two types:

```java
/**
 * Creates an adapter for a loader with the given data-source.
 * @param <R> Resource type
 * @param <T> Intermediate type
 * @param src           Data-source
 * @param loader        Delegate loader
 * @return Data-source loader
 */
static <T, R> Loader<String, R> loader(DataSource src, LoaderAdapter<T, R> loader) {
    return name -> {
        try(final InputStream in = src.open(name)) {
            final T obj = loader.open(in);
            return loader.load(obj);
        }
        catch(IOException e) {
            throw new RuntimeException("Error loading resource: " + name, e);
        }
    };
}
```

We can then define a centralised data-source and combine it with loaders to lookup resources by name:

```java
final DataSource src = DataSource.of("./src/test/resources");

...

final var loader = DataSource.loader(src, new ObjectModelLoader());
final Model model = loader.load("example.obj");
```

This has the benefit of separating the mapping of filenames to resources (handled by the data-sources) from the actual loaders, with the bonus that the checked I/O exceptions can be caught in a single location.

