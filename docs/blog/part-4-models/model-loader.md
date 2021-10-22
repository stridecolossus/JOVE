---
title: Model Loader
---

## Overview

In this chapter we will create an OBJ loader to construct a JOVE representation of the chalet model used in the tutorial.

We will then implement some improvements to reduce the memory footprint of the resultant model and loading times.

---

## Model Loader

### File Format

An OBJ model is specified by a text-based data format consisting of a series of _commands_ that define a static model.  Each line starts with a _command token_ followed by a number of space-delimited arguments.

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

The _face_ command specifies the vertices of a polygon as a tuple of indices delimited by the slash character (the normal and texture coordinate are optional):

| example                   | position | texture | normal |
| -------                   | -------- | ------- | ------ |
| f 1 2 3                   | yes      | no      | no     |
| f 1/2 3/4 5/6             | yes      | yes     | no     |
| f 1/2/3 4/5/6 7/8/9       | yes      | yes     | yes    |
| f 1//2 3//4 5//6          | yes      | no      | yes    |

Example for a simple triangle with texture coordinates:

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

We will implement the minimal functionality required for the chalet model with the following assumptions and constraints on the scope of the loader:

* Face primitives are assumed to be triangles.

* Only the above commands will be supported, others are silently ignored.

* The OBJ format specifies material descriptors that specify texture properties (amongst other features) however we will hard-code the texture image in the demo.

### Vertex Data

Loading an OBJ model consists of the following steps:

1. Load the vertex data (positions, normals, texture coordinates).

2. Load the faces that index into this data to generate the model vertices.

3. Build the resultant JOVE model.

We first implement an intermediate data structure to hold the vertex data:

```java
public class ObjectModel {
    private final List<Point> positions = new VertexComponentList<>();
    private final List<Vector> normals = new VertexComponentList<>();
    private final List<Coordinate2D> coords = new VertexComponentList<>();
}
```

The custom list implementation looks up a vertex component by index (which starts at __one__ and can be negative):

```java
class VertexComponentList<T> extends ArrayList<T> {
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
            throw new IndexOutOfBoundsException(...);
        }
    }
}
```

We also add trivial package-private mutators to add the various vertex components to the model:

```java
void position(Point v) {
    positions.add(v);
}
```

Although not required for the chalet model the OBJ format supports multiple object _groups_ corresponding to one-or-more JOVE models:

```java
public class ObjectModel {
    ...
    private final List<Builder> builders = new ArrayList<>();
    private Builder current;
}
```

We add a local helper to start a new group:

```java
private void add() {
    current = new Model.Builder();
    current.primitive(Primitive.TRIANGLES);
    current.clockwise(true);
    builders.add(current);
}
```

Which is invoked in the constructor to initialise the first group:

```java
public ObjectModel() {
    add();
}
```

### Model Loader

We can now parse the OBJ file as follows:

1. Create an instance of the above transient model.

2. Load each line of the OBJ file (skipping comments and empty lines).

3. Parse each command line and update the model accordingly.

4. Generate the resultant JOVE model(s).

We start with the following class outline:

```java
public class ObjectModelLoader {
    private Set<String> comments = Set.of("#");
    private final Map<String, Parser> parsers = new HashMap<>();
    private final ObjectModel model = new ObjectModel();
}
```

The loader applies the above logic and delegates to a local helper method to parse a line:

```java
public Stream<Model> load(Reader r) throws IOException {
    // Parse OBJ model
    try(LineNumberReader in = new LineNumberReader(r)) {
        try {
            in.lines()
                .map(String::trim)
                .filter(Predicate.not(String::isBlank))
                .filter(Predicate.not(this::isComment))
                .forEach(this::parse);
        }
        catch(Exception e) {
            throw new IOException(...);
        }
    }

    // Construct models
    return model.build();
}
```

The `isComment` method checks for lines that start with a comment token:

```java
private boolean isComment(String line) {
    return comments.stream().anyMatch(line::startsWith);
}
```

In the parse method we first tokenize the line and then delegate to a _parser_ for the command:

```java
private void parse(String line) {
    // Tokenize line
    String[] parts = StringUtils.split(line);
    Parser.trim(parts);

    // Lookup parser
    Parser parser = parsers.get(parts[0]);
    if(parser == null) {
        handler.accept(line);
        return;
    }
    
    // Delegate
    parser.parse(parts, model);
}
```

A parser is defined as follows:

```java
public interface Parser {
    /**
     * Parses the given command.
     * @param args         Arguments (including the command token)
     * @param model        OBJ model
     * @throws NumberFormatException is the data cannot be parsed
     */
    void parse(String[] args, ObjectModel model);
}
```

An unsupported command is delegated to an error handler which is a simple line consumer:

```java
private Consumer<String> handler = line -> { /* Ignored */ };
```

Normally we would throw an exception for an unknown command but it seems more prudent to ignore by default (given the nature of OBJ files).

We also provide the following:

* An `add` method to register a command parser.

* A setter to override the error handler as required.

* The `IGNORE` parser which can be used to explicitly ignore commands.

* The `trim` helper to the parser interface to clean a tokenized array of strings.

### Vertex Components

We note that the process of parsing the various vertex components (v, vn, vt) is the same in all cases:

1. Parse the command arguments to a floating-point array.

2. Construct the relevant domain object from this array.

3. Add the resultant object to the model.

We first implement an _array constructor_ on all the relevant vertex components, for example both points and vectors delegate to the following tuple constructor:

```java
protected Tuple(float[] array) {
    if(array.length != SIZE) throw new IllegalArgumentException(...);
    x = array[0];
    y = array[1];
    z = array[2];
}
```

Next we abstract the common pattern by implementing a new generic parser:

```java
class VertexComponentParser<T extends Vertex.Component> implements Parser {
    private final float[] array;
    private final Function<float[], T> ctor;
    private final BiConsumer<ObjectModel, T> consumer;
}
```

Where:

* _T_ is the component type.

* _ctor_ is a method reference to the _array constructor_ for a given type of component.

* _consumer_ is a reference to a setter method of the model for that vertex component.

To continue the example, we can specify the parser for a vertex position as follows:

```java
new VertexComponentParser<>(Point.SIZE, Point::new, ObjectModel::position);
```

The parse method follows the steps outlined above:

```java
public void parse(String[] args, ObjectModel model) {
    // Convert to array
    for(int n = 0; n < array.length; ++n) {
        array[n] = Float.parseFloat(args[n + 1]);
    }

    // Create object using array constructor
    T value = ctor.apply(array);

    // Add to model
    consumer.accept(model, value);
}
```

Finally we register built-in parsers for the supported vertex components:

```java
public ObjectModelLoader() {
    add("v",  new VertexComponentParser<>(Point.SIZE, Point::new, ObjectModel::position));
    add("vt", new VertexComponentParser<>(2, Coordinate2D::new, ObjectModel::coordinate));
    add("vn", new VertexComponentParser<>(Vector.SIZE, Vector::new, ObjectModel::normal));
}
```

### Face Parser

The parser for the face command (f) iterates over the face vertices (which we assume to be a triangle):

```java
public void parse(String[] args, ObjectModel model) {
    for(int n = 0; n < 3; ++n) {
        // Tokenize face
        String face = args[n + 1];
        ...
    }
}
```

Each vertex is a slash-delimited tuple of indices into the vertex data:

```java
String[] parts = face.split("/");
Parser.trim(parts);
if(parts.length > 3) throw new IllegalArgumentException(...);
```

The vertex position is mandatory:

```java
int v = Integer.parseInt(parts[0]);
```

The middle component of the tuple is an optional texture coordinate:

```java
Integer vt = null;
if((parts.length > 1) && !parts[1].isEmpty()) {
    vt = Integer.parseInt(parts[1]);
}
```

And the last component is the optional normal:

```java
Integer vn = null;
if(parts.length == 3) {
    vn = Integer.parseInt(parts[2]);
}
```

Finally we delegate to a new mutator on the model to add a vertex given the three parsed indices:

```java
public void vertex(int v, Integer vn, Integer vt) {
    // Add vertex position
    final var vertex = new Vertex.Builder();
    vertex.position(positions.get(v));

    // Add optional normal
    if(vn != null) {
        vertex.normal(normals.get(vn));
    }

    // Add optional texture coordinate
    if(vt != null) {
        vertex.coordinate(coords.get(vt));
    }

    // Add vertex
    current.add(vertex.build());
}
```

### Conclusion

To complete the loader we register the remaining command parsers:

```java
add("f", new FaceParser());
add("o", Parser.GROUP);
add("g", Parser.GROUP);
add("s", Parser.IGNORE);
```

The `GROUP` parser delegates to the following method on the model to start a new object group:

```java
public void start() {
    // Ignore if the current group is empty
    if(current.isEmpty()) {
        return;
    }

    // Reset transient model
    positions.clear();
    normals.clear();
    coords.clear();

    // Start new model
    add();
}
```

Notes that some OBJ files start with a group declaration, so we ignore the case where no vertex data has been added to the current group.

Finally we add a method to the model class to generate the resultant JOVE model(s):

```java
public Stream<Model> build() {
    return builders.stream().map(Builder::build);
}
```

---

## Improvements

### Indexed Builder

From the tutorial we know that the chalet model has a large number of duplicate vertices.  An obvious improvement is to introduce an _index buffer_ to the model to reduce the total amount of data (at the expense of a second buffer for the index itself).

We first add an optional index buffer to the model definition:

```java
public interface Model {
    ...
    
    /**
     * @return Whether this is an indexed model
     */
    boolean isIndexed();

    /**
     * @return Index buffer
     */
    Optional<Bufferable> index();
}
```

In the default model implementation the index is a simple integer array:

```java
public class DefaultModel extends AbstractModel {
    private final List<Vertex> vertices;
    private final int[] index;

    @Override
    public boolean isIndexed() {
        return index != null;
    }
}
```

The bufferable object for the index is generated as follows:

```java
public Optional<Bufferable> index() {
    if(index == null) {
        return Optional.empty();
    }

    final Bufferable buffer = new Bufferable() {
        private final int len = index.length * Integer.BYTES;
        
        @Override
        public int length() {
            return len;
        }

        @Override
        public void buffer(ByteBuffer bb) {
            ...
        }
    };
    return Optional.of(buffer);
}
```

The target byte-buffer is transformed to an `IntBuffer` which is more convenient in this case:

```java
public void buffer(ByteBuffer bb) {
    IntBuffer buffer = bb.asIntBuffer();
    if(buffer.isDirect()) {
        for(int n : index) {
            buffer.put(n);
        }
    }
    else {
        buffer.put(index);
    }
}
```

Next we implement a new model builder sub-class that maintains the mapping of vertices to indices:

```java
public class IndexedBuilder extends Builder {
    private final Map<Vertex, Integer> map = new HashMap<>();
    private final List<Integer> index = new ArrayList<>();
}
```

The de-duplication process for a given vertex is:

1. Lookup the index of the vertex (if present).

2. If the vertex already exists in the model add its index to the index buffer.

3. Otherwise add the vertex (__and__ its allocated index) to the model and add a new entry to the mapping.

This is implemented in the overloaded `add` method as follows:

```java
public Builder add(Vertex v) {
    Integer prev = map.get(v);
    if(prev == null) {
        // Add new vertex and register index
        Integer n = vertices.size();
        index.add(n);
        map.put(v, n);
        super.add(v);
    }
    else {
        // Otherwise add index for existing vertex
        index.add(prev);
    }
    return this;
}
```

The indexed builder creates the index array and delegates to the base-class to construct the model:

```java
public DefaultModel build() {
    final int[] array = index.stream().mapToInt(Integer::intValue).toArray();
    return build(array, array.length);
}
```

Finally we add a new method on the buffer class to bind an index buffer to the pipeline:

```java
public Command bindIndexBuffer() {
    require(VkBufferUsage.INDEX_BUFFER);
    return (api, buffer) -> api.vkCmdBindIndexBuffer(buffer, this, 0, VkIndexType.UINT32);
}
```

Notes:

* The index `mapping` assumes we have a decent hashing implementation in the vertex class.

* We refactor the base-class model builder to accept the optional index.

* For the moment we assume 32-bit integer indices, we may want to support other sizes in future.

All this refactoring work reduces the size of the interleaved model from 30Mb to roughly 11Mb (5Mb for the vertex data and 6Mb for the index buffer).  Nice!

### Model Persistence

Although the OBJ loader and new indexed builder are relatively efficient loading the model is now quite slow (even on decent hardware).

We could attempt to optimise the code but this is usually very time-consuming and often actually counter-productive (i.e. complexity often leads to bugs).

Instead we note that as things stand the following steps in the loading process are repeated _every_ time we run the demo:

1. Load and parse the OBJ model.

2. De-duplication of the vertex data.

3. Transformation to NIO buffers.

Ideally we would only perform the above steps _once_ since we are only really interested in the vertex and index bufferable objects.

We therefore introduce a custom persistence mechanism to write a model to the file-system:

```java
public class ModelLoader {
    private static final int VERSION = 1;

    private static void write(Model model, DataOutputStream out) throws IOException {
    }
}
```

Note that we output the model to a `DataOutputStream` as this supports both Java primitives and byte arrays (see below).

In the `write` method we first output the version number of our custom file-format (for later verification):

```java
out.writeInt(VERSION);
```

Next we write the header information of the model:

```java
Header header = model.header();
out.writeUTF(header.primitive().name());
out.writeInt(header.count());
```

Followed by the vertex layout of the model:

```java
List<Layout> layout = header.layout();
out.writeInt(layout.size());
for(Layout c : layout) {
    out.writeInt(c.size());
    out.writeInt(c.bytes());
    out.writeUTF(c.type().getName());
}
```

Note that the _type_ of each component in the layout is output as a string using the `writeUTF` method on the stream.

Next we write the vertex buffer:

```java
writeBuffer(model.vertices(), out);
```

Which uses the following helper to output the length of the data followed by the buffer as a byte-array:

```java
private static void writeBuffer(Bufferable src, DataOutputStream out) throws IOException {
    byte[] array = Bufferable.toArray(src);
    out.writeInt(array.length);
    out.write(array);
}
```

The intermediate byte-array is created using another new helper on the `Bufferable` interface:

```java
static byte[] toArray(Bufferable obj) {
    int len = obj.length();
    ByteBuffer bb = ByteBuffer.allocate(len).order(ORDER);
    obj.buffer(bb);
    return bb.array();
}
```

Finally we output the optional index buffer using the same helper:

```java
var index = model.index();
if(index.isPresent()) {
    writeBuffer(index.get(), out);
}
else {
    out.writeInt(0);
}
```

We _could_ have implemented the model loader using Java serialization, which might have resulted in simpler code but is generally quite nasty to debug, at least our custom format is relatively straight-forward to implement and follow.

### Buffered Models

Obviously when we load this data back we do not want to reuse the existing model class since we only require the underlying buffers.  We therefore introduce a new _buffered model_ implementation that simply composes the two bufferable objects:

```java
public class BufferedModel extends AbstractModel {
    private final Bufferable vertices;
    private final Optional<Bufferable> index;

    ...

    @Override
    public boolean isIndexed() {
        return index.isPresent();
    }

    @Override
    public Bufferable vertices() {
        return vertices;
    }

    @Override
    public Optional<Bufferable> index() {
        return index;
    }
}
```

We add a new public method to the loader class to read a persisted model:

```java
public BufferedModel load(DataInputStream in) throws IOException {
}
```

Here we use the inverse input stream to read Java primitives and byte-arrays.

The loader first verifies that the file-format version is supported:

```java
int version = in.readInt();
if(version > VERSION) {
    throw new UnsupportedOperationException(...);
}
```

Next we load the model header:

```java
Primitive primitive = Primitive.valueOf(in.readUTF());
int count = in.readInt();
```

The process of loading and re-constructing the model layout is slightly more complex since we have to lookup the class of the _type_ by name:

```java
int num = in.readInt();
List<Layout> layout = new ArrayList<>();
for(int n = 0; n < num; ++n) {
    int size = in.readInt();
    int bytes = in.readInt();
    String name = in.readUTF();
    Class<?> type;
    try {
        type = Class.forName(name);
    }
    catch(ClassNotFoundException e) {
        throw new IOException(...);
    }
    layout.add(new Layout(size, type, bytes, true));
}
```

Next we load the vertex and index buffers:

```java
Bufferable vertices = loadBuffer(in);
Bufferable index = loadBuffer(in);
```

And finally we create the new buffered model:

```java
return new BufferedModel(new Header(layout, primitive, count), vertices, Optional.ofNullable(index));
```

The `loadBuffer` helper is the inverse of `writeBuffer` above (with an additional check for an empty buffer):

```java
private static Bufferable loadBuffer(DataInputStream in) throws IOException {
    // Read buffer size
    int len = in.readInt();
    if(len == 0) {
        return null;
    }

    // Load bytes
    byte[] bytes = new byte[len];
    int actual = in.read(bytes);
    if(actual != len) throw new IOException(...);

    // Convert to buffer
    return Bufferable.of(bytes);
}
```

There is still a fair amount of type conversions to/from byte-arrays in this code but the buffered model can now be loaded in a matter of milliseconds.  Result.

### Loader Support

Over the course of this project we have implemented various loaders that share a common pattern but with differing data types:

| implementation        | input type        | resource type     |
| --------------        | ----------        | -------------     |
| Shader.Loader         | InputStream       | Shader            |
| ImageData.Loader      | BufferedImage     | ImageData         |
| ObjectModelLoader     | Reader            | Stream<Model>     |
| ModelLoader           | DataInputStream   | BufferedModel     |

Currently the demo applications are required to implement fiddly I/O code to open the various resources and handle cleanup and checked exceptions.  Ideally we would like to abstract this common pattern by referring to resources by _name_ and encapsulate the nasty code.

We first implement the following abstraction for a general resource loader:

```java
public interface ResourceLoader<T, R> {
    /**
     * Maps an input stream to the intermediate data type.
     * @param in Input stream
     * @return Intermediate data type
     * @throws IOException if the input data cannot be loaded
     */
    T map(InputStream in) throws IOException;

    /**
     * Constructs the resultant resource from the given data.
     * @param data Input data
     * @return Loaded resource
     * @throws IOException if the resource cannot be loaded
     */
    R load(T data) throws IOException;
}
```

Where _R_ is the type of the resource and _T_ is some intermediate data-type.

The purpose of this abstraction is probably best illustrated by an example for the new model loader:

```java
public class ModelLoader implements ResourceLoader<DataInputStream, BufferedModel> {
    @Override
    public DataInputStream map(InputStream in) throws IOException {
        return new DataInputStream(in);
    }
    
    @Override
    public BufferedModel load(DataInputStream in) throws IOException {
        ...
    }
}
```

The `map` method transforms an input-stream to the intermediate data stream, the `load` method itself is unchanged (except for the `@Override`).

Next we introduce a _data source_ which is responsible for opening an input-stream for a given resource by name:

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

We implement convenience factories to create a data-source from the file-system:

```java
static DataSource of(Path dir) {
    if(!Files.exists(dir)) throw new IllegalArgumentException(...);
    return name -> Files.newInputStream(dir.resolve(name));
}

static DataSource of(String dir) {
    return of(Paths.get(dir));
}
```

The final piece of the jigsaw is to create an _adapter_ that combines a resource loader with a data-source:

```java
static <T, R> Function<String, R> of(DataSource src, ResourceLoader<T, R> loader) {
    return name -> {
        try(InputStream in = src.open(name)) {
            T data = loader.map(in);
            return loader.load(data);
        }
        catch(IOException e) {
            throw new RuntimeException("Error loading resource: " + name, e);
        }
    };
}
```

The adapter encapsulates the process of opening and loading the resource and nicely centralises the checked exceptions.

Notes:

* This framework assumes that all resources will be loaded from an underlying input stream (which seems safe enough).

* The adapter is a Java function which is a little awkward but introducing yet another abstraction would probably be overkill.

* The existing resource loaders are refactored accordingly.

In the demo applications we can now configure a common data-source (which also nicely avoids duplication of the path):

```java
@SpringBootApplication
public class RotatingCubeDemo {
    @Bean
    public static DataSource source() {
        return DataSource.of("./src/main/resources");
    }
}
```

And resources can now be loaded much more conveniently:

```java
var loader = ResourceLoader.of(src, new ModelLoader());
Model model = loader.apply("chalet.model");
```

---

## Summary

In this chapter we implemented:

- A loader for an OBJ model.

- Index buffers.

- Model persistence.

- An improved loader abstraction.

