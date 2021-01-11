---
title: Model Loader
---

## Overview

In this chapter we will create a loader for an OBJ model and implement vertex de-duplication.

---

## OBJ Model Loader

### File Format

An OBJ model is a text-based data format consisting of a series of *commands* to define a static model.
Each line starts with a *command token* followed by a number of space-delimited arguments.

The most common commands are:

| command       | arguments     | purpose               | example           |
| -------       | ---------     | -------               | -------           |
| v             | x y x         | vertex position       | v 0.1 0.2 0.3     |
| vn            | x y z         | normal                | vn 0.4 0.5 0.6    |
| vt            | u v           | texture coordinate    | vt 0.7 0.8        |
| f             | see below     | face or triangle      | f 1//3 4//6 7//9  |
| o             | name          | object                | o head            |
| g             | name          | polygon group         | g body            |
| s             | flag          | smoothing group       | s 1               |

The _face_ command specifies the vertices of a polygon as a tuple of indices delimited by the slash character.
Each vertex consists of a position index and optional normal and texture coordinate indices.

| example                   | description                               |
| -------                   | -----------                               |
| f 1 2 3                   | triangle with vertex positions only       |
| f 1/2 3/4 5/6             | triangle also with texture coordinates    |
| f 1/2/3 4/5/6 7/8/9       | also with normals                         |
| f 1//2 3//4 5//6          | normals but no texture coordinates        |

Example:

```
o object
v 0.1 0.2 0.3
v 0.4 0.5 0.6
v 0.7 0.8 0.9
vt 0 0
vt 0 1
vt 1 0
g group
f 1/1 2/2 3/3
```

We will only implement the minimal functionality required for our test models therefore we apply the following assumptions and constraints on the scope of the loader:

- Face primitives are assumed to be triangles.

- Only the above commands will be supported - all others will be ignored.

- The OBJ format supports material descriptors (.MTL) that specify texture properties (amongst others) - for the moment we will simply hard-code the associated texture image.

### Transient Model

Loading an OBJ model consists of two main steps:
1. Load the vertex components (v, vn, vt).
2. Load the object faces (f) that index into this data to build the resultant object.

This implies we need an intermediate data structure to hold the vertex components:

```java
public static class ObjectModel {
    private final List<Point> vertices = new VertexComponentList<>();
    private final List<Vector> normals = new VertexComponentList<>();
    private final List<Coordinate2D> coords = new VertexComponentList<>();
}
```

The custom list implementation retrieves a vertex component using an OBJ index (which starts at one and can be negative):

```java
static class VertexComponentList<T> extends ArrayList<T> {
    @Override
    public T get(int index) {
        if(index > 0) {
            return super.get(index - 1);
        }
        else
        if(index < 0) {
            return super.get(size() + index);
        }
        else {
            throw new IndexOutOfBoundsException("Invalid zero index");
        }
    }
}
```

### Model Loader

We can now parse the OBJ file and construct the model as follows:
1. Create a new transient OBJ model.
2. Load each line of the OBJ file (skipping comments and empty lines).
3. Parse each command line and update the model accordingly.
4. Generate the resultant JOVE model(s).

The loader parses each line of the model and delegates to the local `parse` method:

```java
public class ObjectModelLoader {
    public Model load(Reader r) throws IOException {
        // Create transient model
        final ObjectModel model = new ObjectModel();

        // Parse OBJ model
        try(final LineNumberReader in = new LineNumberReader(r)) {
            in.lines()
                .map(String::trim)
                .filter(Predicate.not(String::isBlank))
                .filter(Predicate.not(this::isComment))
                .forEach(line -> parse(line, model));
        }
        catch(Exception e) {
            throw new IOException(...);
        }

        // Construct models
        return model.build();
    }
}
```

The `isComment` method checks for lines that start with a configured comment token:

```java
public class ObjectModelLoader {
    private Set<String> comments = Set.of("#");

    private boolean isComment(String line) {
        return comments.stream().anyMatch(line::startsWith);
    }

    public void setCommentTokens(Set<String> comments) {
        this.comments = Set.copyOf(comments);
    }
}
```

The `parse` method first separates the command token from the arguments and then delegates to a `Parser` for that command:

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
     * Parser that ignores a command.
     */
    Parser IGNORE = (args, model) -> {
        // Does nowt
    };
}
```

Each parser is registered with the loader indexed by the OBJ command token:

```java
public class ObjectModelLoader {
    private final Map<String, Parser> parsers = new HashMap<>();

    public void add(String token, Parser parser) {
        parsers.put(token, parser);
    }
}
```

### Error Handling

The OBJ format is quite vague and models are notoriously flakey so our loader needs to be robust but not overly stringent.

We add a callback handler with implementations to either ignore unknown commands or throw an exception depending on the application:

```java
public static final Consumer<String> HANDLER_IGNORE = str -> {
};

public static final Consumer<String> HANDLER_THROW = str -> {
    throw new IllegalArgumentException("Unsupported OBJ command: " + str);
};

private Consumer<String> handler = HANDLER_THROW;

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

In addition the application can specify the `IGNORE` parser for a given command.

### Vertex Components

The process for parsing the various vertex components each follows the same pattern:
1. Parse the line to a floating-point array (checking that the number of array elements matches the expected length).
2. Construct the relevant domain object given this array.
3. Add the result to the relevant list in the OBJ model.

We abstract this pattern into the following generic parser:

```java
public class ArrayParser<T> implements Parser {
    private final float[] array;
    private final Function<float[], T> ctor;
    private final Function<ObjectModel, List<T>> mapper;

    public ArrayParser(int size, Function<float[], T> ctor, Function<ObjectModel, List<T>> mapper) {
        this.array = new float[size];
        this.ctor = notNull(ctor);
        this.mapper = notNull(mapper);
    }

    @Override
    public void parse(String[] args, ObjectModel model) {
    }
}
```

Where _ctor_ maps an array to the domain object constructor and _mapper_ retrieves the list for that type of component.

The `parse` method first validates the data and then parses the array elements:

```java
// Validate
if(args.length != size) {
    throw new IllegalArgumentException(...);
}

// Convert to array
for(int n = 0; n < size; ++n) {
    array[n] = Float.parseFloat(args[n].trim());
}
```

Next we construct the domain object given the parsed array and add it to the model:

```java
final T value = ctor.apply(array);
mapper.apply(model).add(value);
```

We add array constructors to the appropriate domain objects, for example:

```java
public class Coordinate2D {
    public Coordinate2D(float[] array) {
        this(array[0], array[1]);
    }
}
```

Finally we register a parser for each vertex component:

```java
public ObjectModelLoader() {
    init();
}

private void init() {
    add("v", new ArrayParser<>(Point.SIZE, Point::new, ObjectModel::vertices));
    add("vn", new ArrayParser<>(Vector.SIZE, Vector::new, ObjectModel::normals));
    add("vt", new ArrayParser<>(Coordinate2D.SIZE, Coordinate2D::new, ObjectModel::coordinates));
    ...
}
```

### Face Parser

The face parser iterates over the vertices of a face (we assume each face is a triangle):

```java
public class FaceParser implements Parser {
    @Override
    public void parse(String[] args, ObjectModel model) {
        // Validate face
        if(args.length != 3) {
            throw new IllegalArgumentException("Expected triangle face");
        }

        // Parse vertices for this face
        for(String face : args) {
            ...
        }
    }
}
```

Each vertex is comprised of a slash-delimited array that specifies the components of the vertex:

```java
// Tokenize face
final String[] parts = face.trim().split("/");
if(parts.length > 3) throw new IllegalArgumentException("Invalid face: " + face);

// Clean
for(int n = 0; n < parts.length; ++n) {
    parts[n] = parts[n].trim();
}
```

Which are then parsed to integer indices (only the vertex position is mandatory) and added to the model:

```java
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
```

The `vertex` method looks up the components and constructs a `Vertex` which is added to the builder for the current group (covered in the following section):

```java
public void vertex(int v, Integer n, Integer tc) {
    // Build vertex
    final var vertex = new Vertex.Builder();
    vertex.position(vertices.get(v));

    // Add optional normal
    if(n != null) {
        vertex.normal(normals.get(n));
    }

    // Add optional texture coordinate
    if(tc != null) {
        vertex.coords(coords.get(tc));
    }

    // Add to model
    final Model.Builder builder = current();
    builder.add(vertex.build());
}
```

Finally we register the default face parser in the main loader class:

```java
private void init() {
    ...
    add("f", new FaceParser());
}
```

### Groups

A single OBJ file can be comprised of multiple _objects_ which results in a list of JOVE models.

The transient OBJ model maintains a list of builders for each object and initialises the first in the constructor:

```java
public class ObjectModel {
    private final Deque<Model.Builder> builders = new LinkedList<>();

    public ObjectModel() {
        add();
    }

    /**
     * Adds a new model for the next group.
     */
    private void add() {
        final Model.Builder next = new Model.Builder();
        next.primitive(Primitive.TRIANGLES);
        builders.add(next);
    }
}
```

A new object is started by the following command:

```java
public interface Parser {
    /**
     * Parser for a new object command (either {@code o} or {@code g}).
     * @see ObjectModel#start()
     */
    Parser GROUP = (args, model) -> model.start();
}
```

Which delegates to the `start` method:

```java
public void start() {
    // Ignore if the current group is empty
    if(isEmpty()) {
        return;
    }

    // Initialise the vertex layout for the previous model
    init();
    
    // Reset transient model
    vertices.clear();
    normals.clear();
    coords.clear();

    // Start new model
    add();
}
```

The `init` method initialises the vertex layout of the _previous_ model builder:

```java
private void init() {
    // Determine vertex layout for the current object group
    final var layout = new ArrayList<Vertex.Component>();
    layout.add(Vertex.Component.POSITION);
    if(!normals.isEmpty()) {
        layout.add(Vertex.Component.NORMAL);
    }
    if(!coords.isEmpty()) {
        layout.add(Vertex.Component.TEXTURE_COORDINATE);
    }

    // Initialise current model
    final Model.Builder builder = current();
    builder.layout(new Vertex.Layout(layout));
}
```

The resultant model(s) are constructed in the `build` method:

```java
public Stream<Model> build() {
    if(isEmpty()) throw new IllegalStateException("Model is empty");
    init();
    return builders.stream().map(Model.Builder::build);
}
```

Note that we also invoke `init` here to initialise the vertex layout of the last model before construction.

Finally we register parsers for the relevant object and group commands:

```java
private void init() {
    ...
    add("o", Parser.GROUP);
    add("g", Parser.GROUP);
    add("s", Parser.IGNORE);
}
```

---

## Vertex Duplication

### Indexed Builder

We know that the OBJ model that we will be using in our demo contains a large number of duplicate vertices which we have ignored until now.
The obvious next step is remove the duplicates and introduce an *index buffer* to the model building process.
This will reduce the total amount of data in the vertex buffer (at the expense of a second buffer for the index).

We implement the following model builder sub-class that maintains a map of the vertex indices so we can test for duplicates:

```java
public static class IndexedBuilder extends Builder {
    private final List<Integer> index = new ArrayList<>();
    private final Map<Vertex, Integer> map = new HashMap<>();
}
```

The over-ridden `add` method intercepts duplicates and stores the index of new vertices:

```java
public IndexedBuilder add(Vertex vertex) {
    // Lookup existing vertex index
    final Integer prev = map.get(vertex);

    if(prev == null) {
        // Add new vertex
        final int idx = count();
        map.put(vertex, idx);
        index.add(idx);
        super.add(vertex);
    }
    else {
        // Add existing vertex
        index.add(prev);
    }

    return this;
}
```

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

And implement the command to bind the index buffer on the `VulkanBuffer` class:

```java
public Command bindIndexBuffer() {
    return (api, buffer) -> api.vkCmdBindIndexBuffer(buffer, this.handle(), 0, VkIndexType.VK_INDEX_TYPE_UINT32);
}
```

All this refactoring work reduces the size of the interleaved model from 30Mb to roughly 11Mb (5Mb for the vertex data and 6Mb for the index buffer).

Result.

> Initially we tried to protect the buffers using `asReadOnlyBuffer()` but this seemed to break our unit-tests (equality of buffers is complex) and caused issues when we came to integration so they are exposed as mutable for the moment.

### Model Persistence

One problem with the OBJ loader is it quite slow even though we are developing on a decent gaming machine.

One obvious observation is that the following parts of the loading process are repeated *every* time we execute the demo:

1. Load and parse the OBJ model.

2. De-duplication.

3. Generation of the NIO buffers for the vertex data and the index.

However we _really_ only need the resultant NIO buffers - therefore we will implement a persistence mechanism for a model so that we only need to do the above _once_ for a given model.

We create a new `ModelLoader` class that will be responsible for reading and writing a buffered model:

```java
public static class ModelLoader {
    public Model load(DataInputStream in) throws IOException {
    }

    public void write(Model model, DataOutputStream out) throws IOException {
    }
}
```

We use data streams so that we can persist Java primitives the byte arrays for the VBO and index.

The model primitive is output as a UTF string:

```java
out.writeUTF(model.primitive().name());
```

The layout is a concatenated string representation of the vertex components:

```java
private static final String DELIMITER = "-";

...

final String layout = model
    .layout()
    .components()
    .stream()
    .map(Enum::name)
    .collect(joining(DELIMITER));

out.writeUTF(layout);
```

Finally we write the vertex and index buffers:

```java
// Write VBO
out.writeInt(model.count());
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

The `write` helper outputs the length of the buffer and then writes it as an array:

```java
private static void write(ByteBuffer bb, DataOutputStream out) throws IOException {
    final byte[] bytes = new byte[bb.limit()];
    bb.get(bytes);
    out.writeInt(bytes.length);
    out.write(bytes);
}
```

> The handling of the optional index buffer is a bit ugly but things get messy when we mix optional values, lambdas and checked exceptions so it will have to do.

We obviously do not want to recreate an instance of the _existing_ model implementation, so we will create a new _buffered model_ with the VBO and index as members. 
The existing model class is refactored as an interface and we add a skeleton implementation shared by both:

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
        ...
    }
}
```

The model is loaded and created as follows:

```java
private static final int VERSION = 1;

public Model load(DataInputStream in) throws IOException {
    // Load and verify file format version
    final int version = in.readInt();
    if(version > VERSION) {
        throw new UnsupportedOperationException(...);
    }

    // Load primitive
    final Primitive primitive = Primitive.valueOf(in.readUTF());
    
    // Load layout
    final var layout = Arrays.stream(in.readUTF().split(DELIMITER)).map(Vertex.Component::valueOf).collect(toList());
    
    // Load buffers
    final int count = in.readInt();
    final ByteBuffer vertices = loadBuffer(in);
    final ByteBuffer index = loadBuffer(in);
    
    // Create model
    return new BufferedModel(primitive, new Vertex.Layout(layout), vertices, index, count);
}
```

The `loadBuffer` helper loads to an NIO buffer and handles the case of the optional index:

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

We also introduce a `VERSION` for our custom file format (and modify the write method accordingly).

We output the buffered model once and then modify the demo to read the persisted data thereafter.
There is still a lot of conversions of byte buffers to/from arrays but our model is now loaded in a matter of milliseconds - Nice!

---

## Loader Support

We now have several loaders that all perform roughly the same functionality but with differing data types:

| implementation        | input type        | resource type     |
| --------------        | ----------        | -------------     |
| ObjectModelLoader     | Reader            | Stream<Model>     |
| ShaderLoader          | InputStream       | Shader            |
| ImageData.Loader      | BufferedImage     | ImageData         |
| ModelLoader           | DataInputStream   | BufferedModel     |

As things stand the demo applications are required to implement fiddly I/O code to open the various resources and handle resource cleanup and exceptions.
Ideally we would like the loaders to encapsulate this logic such that the application simply refers to resources by filename.

We first create an abstraction for a general loader:

```java
@FunctionalInterface
public interface ResourceLoader<T, R> {
    /**
     * Loads a resource.
     * @param in Input data
     * @return Loaded resource
     * @throws IOException if the resource cannot be loaded
     */
    R load(T in) throws IOException;
}
```

Where `<T>` is the input type (e.g. `DataInputStream` for the model loader above) and `<R>` is the resource type.

We adapt this interface with a template implementation that converts a general input-stream to the loader-specific input type:

```java
abstract class Adapter<T, R> implements ResourceLoader<T, R> {
    /**
     * Maps the given input-stream to the input type for this loader.
     * @param in Input-stream
     * @return Input type
     * @throws IOException if the stream cannot be opened
     */
    protected abstract T map(InputStream in) throws IOException;
}
```

We can now refactor the various loaders accordingly, for example:

```java
public static class ModelLoader extends ResourceLoader.Adapter<DataInputStream, Model> {
    @Override
    protected DataInputStream map(InputStream in) throws IOException {
        return new DataInputStream(in);
    }

    @Override
    public Model load(DataInputStream in) throws IOException {
        ...
    }
}
```

This allows the loaders to expose the `load` method for easier testability but hides the input type mapping logic.

Next we introduce a _data source_ that allows the application to define a centralised resource factory:

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

With factory methods to create a data-source from a given directory:

```java
static DataSource of(Path dir) {
    if(!Files.exists(dir)) throw new IllegalArgumentException(...);
    return name -> Files.newInputStream(dir.resolve(name));
}

static DataSource of(String dir) {
    return of(Paths.get(dir));
}
```

The final piece of the jigsaw is the following factory method on the `ResourceLoader` that combines an adapter and a data-source:

```java
static <T, R> ResourceLoader<String, R> of(DataSource src, Adapter<T, R> loader) {
    return name -> {
        try(final InputStream in = src.open(name)) {
            final T input = loader.map(in);
            return loader.load(input);
        }
        catch(IOException e) {
            throw new RuntimeException("Error loading resource: " + name, e);
        }
    };
}
```

An application can now define a data-source and combine it with the various loaders to load resources by filename:

```java
// Create data-source
DataSource src = DataSource.of("./src/test/resources");

// Load model
var modelLoader = ResourceLoader.of(src, new ModelLoader());
Model model = modelLoader.load("chalet.model");

// Load texture image
var imageLoader = ResourceLoader.of(src, new ImageData.Loader());
ImageData image = imageLoader.load("chalet.jpg");
```

This separates the mapping of filenames to resources from the actual loaders and centralises the common loader pattern.
We also have the added bonus that the handling of the checked I/O exceptions is centralised in a single location.

---

## Summary

In this chapter we implemented:

- A loader for an OBJ model.

- Index buffers.

- Model persistence.

- An improved loader abstraction.

