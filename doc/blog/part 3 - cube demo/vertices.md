# Tuple

```java
public class Tuple implements Bufferable {
	public static final int SIZE = 3;

	public final float x, y, z;

	protected Tuple(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void buffer(FloatBuffer buffer) {
		buffer.put(x).put(y).put(z);
	}
}
```

# Point

```java
public final class Point extends Tuple {
	/**
	 * Origin point.
	 */
	public static final Point ORIGIN = new Point(0, 0, 0);

	public Point(float x, float y, float z) {
		super(x, y, z);
	}
}
```

# Vertex

```java
public interface Vertex {
	Point position();
	Vector normal();
	TextureCoordinate coords();
	Colour colour();

	/**
	 * Default implementation.
	 */
	record DefaultVertex(Point position, Vector normal, TextureCoordinate coords, Colour colour) implements Vertex {
	}

	class Builder {
		...
		public Vertex build() {
			return new DefaultVertex(pos, normal, coords, col);
		}
	}
}
```

# Component 

```java
enum Component {
	POSITION(Point.SIZE, Vertex::position),
	NORMAL(Vector.SIZE, Vertex::normal),
	TEXTURE_COORDINATE(TextureCoordinate.Coordinate2D.SIZE, Vertex::coords),
	COLOUR(Colour.SIZE, Vertex::colour);

	...

	public void buffer(Vertex vertex, FloatBuffer fb) {
		mapper.apply(vertex).buffer(fb);
	}
}

class Layout {
	private final List<Component> layout;
	private final int size;

	public Layout(List<Component> layout) {
		this.layout = List.copyOf(layout);
		this.size = layout.stream().mapToInt(Component::size).sum();
	}

	...

	/**
	 * Helper - Creates and populates an interleaved buffer containing the given vertex data.
	 * @param vertices Vertex data
	 * @return New buffer
	 */
	public ByteBuffer buffer(List<Vertex> vertices) {
		// Create buffer
		final ByteBuffer bb = BufferFactory.byteBuffer(size * Float.BYTES * vertices.size());

		// Buffer vertices
		final FloatBuffer fb = bb.asFloatBuffer();
		vertices.forEach(v -> buffer(v, fb));

		return bb;
	}

	/**
	 * Buffers the components of a vertex to the given buffer according to this layout.
	 * @param vertex		Vertex
	 * @param buffer		Output buffer
	 */
	public void buffer(Vertex vertex, FloatBuffer buffer) {
		layout.forEach(c -> c.buffer(vertex, buffer));
	}
}
```

# Example

```java
// Build triangle vertices
final Vertex[] vertices = {
		new Vertex.Builder().position(new Point(0, -0.5f, 0)).colour(new Colour(1, 0, 0, 1)).build(),
		new Vertex.Builder().position(new Point(0.5f, 0.5f, 0)).colour(new Colour(0, 1,  0, 1)).build(),
		new Vertex.Builder().position(new Point(-0.5f, 0.5f, 0)).colour(new Colour(0, 0, 1, 1)).build(),
};

// Define vertex layout
final Vertex.Layout layout = new Vertex.Layout(List.of(Vertex.Component.POSITION, Vertex.Component.COLOUR));

// Buffer vertices
final ByteBuffer bb = layout.buffer(Arrays.asList(vertices));
```
