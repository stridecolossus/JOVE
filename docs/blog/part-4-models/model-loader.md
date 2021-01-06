---
title: Models and the Depth Buffer
---

## Overview

In this chapter we will load and render a static OBJ model and implement a _depth buffer_ attachment.

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
     * Parser that ignores the arguments.
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

Next we construct the domain object given the parsed array and add it to the model using the _mapper_ to lookup the relevant component list:

```java
// Create object using array constructor
final T value = ctor.apply(array);

// Add to transient model
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
    add("v", Parser.of(Point.SIZE, Point::new, ObjectModel::vertices));
    add("vn", Parser.of(Vector.SIZE, Vector::new, ObjectModel::normals));
    add("vt", Parser.of(Coordinate2D.SIZE, Coordinate2D::new, ObjectModel::coordinates));
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

```
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

This method also initialises the vertex layout of the _previous_ model builder:

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

## Indexed Models

### Vertex De-Duplication

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

The over-ridden `add` method intercepts duplicates and registers the index for new unique vertices:

```java
public IndexedBuilder add(Vertex vertex) {
    // Lookup existing vertex index
    final Integer prev = map.get(vertex);
    final int idx = prev == null ? count() : prev;

    // Add new vertices
    if(prev == null) {
        map.put(vertex, idx);
        super.add(vertex);
    }

    ...

    return this;
}
```

The _auto_ setting is used to automatically add an index for each vertex which we make use of in the OBJ loader:

```java
private boolean auto = true;

/**
 * Sets whether to automatically add an index for each vertex (default is {@code true}).
 * @param auto Whether to automatically add indices
 */
public IndexedBuilder setAutoIndex(boolean auto) {
    this.auto = auto;
    return this;
}

public IndexedBuilder add(Vertex vertex) {
    ...
    
    // Add index
    if(auto) {
        add(idx);
    }

    return this;
}
```

We also add new methods to explicitly add or lookup indices:

```java
public Builder add(int index) {
    Check.zeroOrMore(index);
    if(index >= count()) throw new IndexOutOfBoundsException(...);
    this.index.add(index);
    return this;
}

public int indexOf(Vertex vertex) {
    final Integer index = map.get(vertex);
    if(index == null) throw new IllegalArgumentException(...);
    return index;
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

Finally we refactor the OBJ loader to use the new indexed builder and we also we add the `setAutoIndex()` option to the indexed builder to automatically add an index for each vertex.

> Initially we tried to protect the buffers using `asReadOnlyBuffer()` but this seemed to break our unit-tests (equality of buffers is complex) and caused issues when we came to integration so they are exposed as mutable for the moment.

All this work reduces the size of interleaved model from 30Mb to roughly 11Mb (5Mb for the vertex data and 6Mb for the index buffer).

Result.

### Model Persistence

When we debug and test the new loader we find that it is now quite slow (despite the fact we are developing on a gaming machine with a decent processor and fast SSD).

One obvious observation is that much of the loading process is repeated *every* time we execute the demo:
1. Load and parse the OBJ file to the transient model.
2. Build the de-duplicated model from the transient model.
3. Generate the interleaved vertex data.
4. Construct the vertex and index buffers.

We really only need to do the above *once* therefore we implement a persistence mechanism for a model.

We create the new `ModelLoader` class that will be responsible for reading and writing a buffered model:

```java
public static class ModelLoader {
    public void write(Model model, DataOutputStream out) throws IOException {
    }

    public Model load(DataInputStream in) throws IOException {
    }
}
```

We use data streams so that we can persist Java primitives and byte arrays.

#### Persisting a Model

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

The `write()` helper outputs the length of a byte buffer and then writes it as an array:

```java
private static void write(ByteBuffer bb, DataOutputStream out) throws IOException {
    final byte[] bytes = new byte[bb.limit()];
    bb.get(bytes);
    out.writeInt(bytes.length);
    out.write(bytes);
}
```

The handling of the optional index buffer is a bit ugly but things get messy when we mix optional values, lambdas and checked exceptions so it will have to do.

#### Reading a Persisted Model

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
    
    // Load vertex count
    final int count = in.readInt();
    
    // Load buffers
    final ByteBuffer vertices = loadBuffer(in);
    final ByteBuffer index = loadBuffer(in);
    
    // Create model
    return new BufferedModel(primitive, new Vertex.Layout(layout), vertices, index, count);
}
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

We also introduce a `VERSION` for our custom file format (and modify the write method accordingly).

#### Conclusion

We output the model once and then modify the demo to read the buffered model thereafter:

```java
var loader = DataSource.loader(src, new ModelLoader());
Model model = loader.load("chalet.model");
```

There is still a lot of conversions of byte buffers to/from arrays but our model is now loaded in a matter of milliseconds - Nice!

---

## Loader Support

During the creation of the OBJ and buffered model loaders we took a detour to refactor the various loaders we had implemented so far (including images and shaders).

First we created a _loader_ abstraction:

```java
/**
 * A <i>loader</i> defines a mechanism for loading a resource.
 * @param <T> Input type
 * @param <R> Resource type
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

And defined a base-class adapter that is required to implement the `open()` method to map an arbitrary resource from an input stream:

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

---

## Depth Buffers

### Integration #1

After cloning the code from the previous demo we first load the buffered model and transfer the vertex and index buffers to the hardware:

```java
// Load model
final var loader = DataSource.loader(src, new ModelLoader());
final Model model = loader.load("chalet.model");

// Load VBO
final Command.Pool copyPool = Command.Pool.create(dev.queue(transfer));
final VertexBuffer vbo = loadBuffer(dev, model.vertices(), VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, copyPool);

// Load index buffer
final VertexBuffer index = loadBuffer(dev, model.index().get(), VkBufferUsageFlag.VK_BUFFER_USAGE_INDEX_BUFFER_BIT, copyPool);
```

The `loadBuffer()` helper wraps up the code to copy data to the hardware via a staging buffer.

Next we need to add a new command to the buffer class to bind an index buffer:

```java
public Command bindIndexBuffer() {
    return (api, buffer) -> api.vkCmdBindIndexBuffer(buffer, this.handle(), 0, VkIndexType.VK_INDEX_TYPE_UINT32);
}
```

And we rename the existing method to `bindVertexBuffer()` to differentiate the commands.  We also get around to renaming the class to `VulkanBuffer` since it is not really just for vertex data.

Finally we add a helper for the drawing command:

```java
public interface DrawCommand extends Command {
    /**
     * Creates a drawing command.
     * @param model Model to draw
     * @return Drawing command
     */
    static DrawCommand of(Model model) {
        if(model.index().isPresent()) {
            return (api, handle) -> api.vkCmdDrawIndexed(handle, model.count(), 1, 0, 0, 0);
        }
        else {
            return (api, handle) -> api.vkCmdDraw(handle, model.count(), 1, 0, 0);
        }
    }
    // TODO - instancing, offset, etc
}
```

The rendering sequence now looks like this:

```java
.begin()
    .add(pass.begin(buffer))
    .add(pipeline.bind())
    .add(vbo.bindVertexBuffer())
    .add(index.bindIndexBuffer())
    .add(descriptor.bind(pipelineLayout))
    .add(DrawCommand.of(model))
    .add(RenderPass.END_COMMAND)
.end();
```

We strip the code that applied the rotation and see what happens - and what we get is a mess:

- the model is rendered but it looks sort of *inside out*
- the grass is obviously on the roof and vice-versa
- we are seeing bits of the model on top of each other

There are several issues here but the most pressing is the fact that we now need a *depth buffer* so that fragments are not rendered arbitrarily overlapping each other.

### Adding a Depth Buffer

#### Depth Buffer Attachment

We first add a second attachment to the render pass for the depth buffer:

```java
final RenderPass pass = new RenderPass.Builder(dev)
   ...
    .attachment()
        .format(VkFormat.VK_FORMAT_D32_SFLOAT)
        .load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
        .finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
        .build()
    .subpass()
        .colour(0, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
        .depth(1)
        .build()
    .build();
```

The format of the depth buffer attachment is hard-coded to one that is commonly available on most Vulkan implementations, but we make a note to come back and add code to properly select a format appropriate to the hardware.

Next we add a new method to the sub-pass builder to register a depth-buffer attachment:

```java
public SubpassBuilder depth(int index) {
    info.pDepthStencilAttachment = reference(index, VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
    return this;
}
```

We factor out the code that is common to both types of attachment we have implemented so far and refactor accordingly:

```java
private VkAttachmentReference reference(int index, VkImageLayout layout) {
    Check.zeroOrMore(index);
    Check.notNull(layout);
    if(index >= attachments.size()) throw new IllegalArgumentException(...);
    if(layout == VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED) throw new IllegalArgumentException(...);

    // Create reference
    final var ref = new VkAttachmentReference();
    ref.attachment = index;
    ref.layout = layout;
    return ref;
}
```

#### Depth-Stencil Pipeline Stage

Up until now we have not needed to specify the optional depth-stencil pipeline stage and the relevant field in the create descriptor was set to `null`.

We implement a new nested builder to configure this stage (which turns out to be fairly trivial):

```java
public class DepthStencilStageBuilder extends AbstractPipelineBuilder<VkPipelineDepthStencilStateCreateInfo> {
    private final VkPipelineDepthStencilStateCreateInfo info = new VkPipelineDepthStencilStateCreateInfo();

    public DepthStencilStageBuilder() {
        enable(false);
        write(true);
        compare(VkCompareOp.VK_COMPARE_OP_LESS);

        // TODO
        info.depthBoundsTestEnable = VulkanBoolean.FALSE;
        info.minDepthBounds = 0;
        info.maxDepthBounds = 1;

        // TODO - stencil
        info.stencilTestEnable = VulkanBoolean.FALSE;
    }

    /**
     * Sets whether depth-testing is enabled (default is {@code false}).
     * @param enabled Whether depth-test is enabled
     */
    public DepthStencilStageBuilder enable(boolean enabled) {
        info.depthTestEnable = VulkanBoolean.of(enabled);
        return this;
    }

    /**
     * Sets whether to write to the depth buffer (default is {@code true}).
     * @param write Whether to write to the depth buffer
     */
    public DepthStencilStageBuilder write(boolean write) {
        info.depthWriteEnable = VulkanBoolean.of(write);
        return this;
    }

    /**
     * Sets the depth-test comparison function (default is {@link VkCompareOp#VK_COMPARE_OP_LESS}).
     * @param op Depth-test function
     */
    public DepthStencilStageBuilder compare(VkCompareOp op) {
        info.depthCompareOp = notNull(op);
        return this;
    }

    @Override
    protected VkPipelineDepthStencilStateCreateInfo result() {
        return info;
    }
}
```

Finally we modify the pipeline configuration to include the depth test:

```java
final Pipeline pipeline = new Pipeline.Builder(dev)
    ...
    .depth()
        .enable(true)
        .build()
    ...
    .build();
```

#### Creating the Depth Buffer

Unlike the swapchain images we need to create the depth buffer image ourselves:

```java
final Image depthImage = new Image.Builder(dev)
    .aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT)
    .extents(extents)
    .format(VkFormat.VK_FORMAT_D32_SFLOAT)
    .tiling(VkImageTiling.VK_IMAGE_TILING_OPTIMAL)
    .usage(VkImageUsageFlag.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT)
    .property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
    .build();

final View depth = new View.Builder(dev)
    .image(depthImage)
    .subresource()
        .aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT)
        .build()
    .build();
```

Notes:

- The extents of the image should be same as the swapchain images.
- The format is again hard-coded for the moment.
- The store operation is set to ignore since we don't use the buffer after rendering.
- We leave the old layout as undefined since we don't care about the previous contents.
- We do not need to transition the depth buffer image as this is handled by Vulkan during the render pass.

The depth buffer view is then added to each frame buffer object we create.
The same depth buffer can safely be used in each frame because only a single sub-pass will be running at any one time in our bodged render loop.

### Clearing the Attachments

The final step for the depth buffer is to specify how it is cleared before each render pass.

Prior to this stage we had hard-coded a clear value for a single colour attachment in the command for starting the render pass.
We need to make this more general so we can support an arbitrary number of colour and depth attachments.

This involves the following:
1. factoring out the code that actually populates a clear value.
2. make the clear value a property of the attachment view.
3. re-factor the render command accordingly.

#### Clear Values

Firstly we implement a new domain class to represent a clear value:

```java
public abstract class ClearValue {
    private final VkImageAspectFlag aspect;
    private final Object arg;

    /**
     * Constructor.
     * @param aspect        Expected image aspect
     * @param arg           Clear argument
     */
    private ClearValue(VkImageAspectFlag aspect, Object arg) {
        this.aspect = notNull(aspect);
        this.arg = notNull(arg);
    }

    /**
     * @return Expected image aspect
     */
    public VkImageAspectFlag aspect() {
        return aspect;
    }

    /**
     * @return Population function
     */
    public abstract void populate(VkClearValue value);
}
```

This is a skeleton implementation where the `populate()` method fills the relevant field(s) in the `VkClearValue` array when we create the render command.
The `aspect()` method allows us to check that we are applying the correct type of clear value for a given attachment.

We add factory methods to create a clear value for the two cases - colour attachments:

```java
/**
 * Creates a clear value for a colour attachment.
 * @param col Colour
 * @return New colour attachment clear value
 */
public static ClearValue of(Colour col) {
    return new ClearValue(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT, col) {
        @Override
        public void populate(VkClearValue value) {
            value.setType("color");
            value.color.setType("float32");
            value.color.float32 = col.toArray();
        }
    };
}
```

and the depth attachment:

```java
/**
 * Creates a clear value for a depth buffer attachment.
 * @param depth Depth value 0..1
 * @return New depth attachment clear value
 * @throws IllegalArgumentException if the depth is not a valid 0..1 value
 */
public static ClearValue depth(float depth) {
    Check.isPercentile(depth);
    return new ClearValue(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT, depth) {
        @Override
        public void populate(VkClearValue value) {
            value.setType("depthStencil");
            value.depthStencil.depth = depth;
            value.depthStencil.stencil = 0;
        }
    };
}
```

> Introducing this new functionality should have been easy but we had a lot of head-scratching when we first tested this code with JNA throwing the infamous `Invalid memory access` error.  Eventually we realised that we are actually dealing with **unions** and not structures here!  Presumably our original code only worked because we were dealing with a single attachment and Vulkan simply ignored the 'extra' data of the structures.  We manually modified the relevant Vulkan types to unions and used the JNA `setType()` method to 'select' the relevant property.  As far as we can tell this is the **only** instance in the whole Vulkan API that uses a union!

Finally we add default values for both cases and a static helper that can be used to determine the default clear value for a given attachment:

```java
public static final ClearValue COLOUR = of(Colour.BLACK);
public static final ClearValue DEPTH = depth(1);

/**
 * Determines the default clear value for the given image aspects.
 * @param aspects Image aspects
 * @return Default clear value or {@code null} if not applicable
 */
public static ClearValue of(Set<VkImageAspectFlag> aspects) {
    if(aspects.contains(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)) {
        return COLOUR;
    }
    else
    if(aspects.contains(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT)) {
        return DEPTH;
    }
    else {
        return null;
    }
}
```

#### Attachment Clear Value

The clear value for a given attachment is now a property of the image view:

```java
public class View {
    private ClearValue clear;
    
    public ClearValue clear() {
        return clear;
    }
    
    /**
     * Sets the clear value for this attachment.
     * @param clear Clear value
     * @throws IllegalArgumentException if the clear value is incompatible with this view
     */
    public void clear(ClearValue clear) {
        if(!image.descriptor().aspects().contains(clear.aspect())) {
            throw new IllegalArgumentException(...)
        }
        this.clear = notNull(clear);
    }
}
```

This allows us to set the clear value for an attachment in the view builder (or initialise it to an appropriate default):

```java
private ClearValue clear;

public View build() {
    ...

    // Create image view
    final View view = new View(handle.getValue(), image, dev);

    // Init clear value
    if(clear == null) {
        clear = ClearValue.of(image.descriptor().aspects());
    }
    view.clear = clear;

    return view;
}
```

In particular we can now refactor the swapchain code to initialise the clear colour when we construct the views which is much more convenient and centralised:

```java
final SwapChain chain = new SwapChain.Builder(surface)
    .count(2)
    .format(format)
    .space(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
    .clear(new Colour(0.3f, 0.3f, 0.3f, 1))
    .build();
```

For our demo we leave the depth buffer to use the default clear value.

Finally we refactor the command factory for the render pass to use the above and remove the temporary code we used to clear the colour attachment:

```java
public Command begin(FrameBuffer buffer, Rectangle extent) {
    // Create descriptor
    final VkRenderPassBeginInfo info = new VkRenderPassBeginInfo();
    info.renderPass = this.handle();
    info.framebuffer = buffer.handle();
    info.renderArea = buffer.extents().toRect2D();

    // Map attachments to clear values
    final Collection<ClearValue> values = buffer.attachments().stream().map(View::clear).collect(toList());

    // Init clear values
    info.clearValueCount = values.size();
    info.pClearValues = VulkanStructure.populate(VkClearValue::new, values, ClearValue::populate);

    // Create command
    return (lib, handle) -> lib.vkCmdBeginRenderPass(handle, info, VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE);
}
```

### Integration #2

When we run our demo after this painful refactoring exercise things look somewhat better, the geometry is no longer overlapping thanks to the depth buffer.

However we still need to solve the other problems.

#### Inverting Texture Coordinates

For the problem of the upside-down texture coordinates we *could* simply flip the texture image or fiddle the texture coordinates in the shader - but neither of these solves the actual root problem (and inverting the image would make loading slower).  

Therefore we add a new property to the transient OBJ model that flips the coordinates in the Y direction *once* during loading:

```java
public static class ObjectModel {
    private boolean flip = true;

    ...

    /**
     * Sets whether to vertically flip texture coordinates (default is {@code true}).
     * @param flip Whether to vertically flip coordinates
     */
    public void flip(boolean flip) {
        this.flip = flip;
    }

    protected void coord(Coordinate2D coords) {
        if(flip) {
            this.coords.add(new Coordinate2D(coords.u, -coords.v));
        }
        else {
            this.coords.add(coords);
        }
    }
}
```

#### Rasterizer Pipeline Stage

The inside-out problem is due to the fact that the triangles in the OBJ model are opposite to the default winding order.

We take a small detour to fully implement the builder for the rasterizer pipeline stage (which again is pretty simple):

```java
public class RasterizerStageBuilder extends AbstractPipelineBuilder<VkPipelineRasterizationStateCreateInfo> {
    private boolean depthClampEnable;
    private boolean rasterizerDiscardEnable;
    private VkPolygonMode polygonMode = VkPolygonMode.VK_POLYGON_MODE_FILL;
    private VkCullModeFlag cullMode = VkCullModeFlag.VK_CULL_MODE_BACK_BIT;
    private VkFrontFace frontFace = VkFrontFace.VK_FRONT_FACE_COUNTER_CLOCKWISE;
    private boolean depthBiasEnable;
    private float depthBiasConstantFactor;
    private float depthBiasClamp;
    private float depthBiasSlopeFactor;
    private float lineWidth = 1;

    @Override
    protected VkPipelineRasterizationStateCreateInfo result() {
        final var info = new VkPipelineRasterizationStateCreateInfo();
        ...
        return info;
    }
}
```

And we swap the face-culling property (alternatively we could swap the winding order, either works):

```java
final Pipeline pipeline = new Pipeline.Builder(dev)
    ...
    .rasterizer()
        .cullMode(VkCullModeFlag.VK_CULL_MODE_FRONT_BIT)
        .build()
    ...
```

#### Conclusion

We are also viewing the model from above so we add a temporary rotation so we see it from the side and finally we get the following:

![Chalet Model](chalet.png)

Ta-da!

---

## Summary

In this chapter we implemented a simple OBJ model loader and a depth buffer attachment to correctly render the model.
