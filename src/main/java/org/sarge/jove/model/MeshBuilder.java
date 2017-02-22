package org.sarge.jove.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.geometry.BoundingBox;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Builder for a {@link BufferedMesh}.
 * @author Sarge
 */
public class MeshBuilder {
	protected final MeshLayout layout;
	protected final List<Vertex> vertices = new ArrayList<>();

	/**
	 * Constructor.
	 * @param layout Mesh descriptor
	 */
	public MeshBuilder(MeshLayout layout) {
		Check.notNull(layout);
		this.layout = layout;
	}

	/**
	 * @return Mesh layout for this builder
	 */
	public MeshLayout getLayout() {
		return layout;
	}

	/**
	 * @return Vertex data
	 */
	public List<Vertex> getVertices() {
		return vertices;
	}

	/**
	 * @return Number of rendered vertices
	 */
	public int getVertexCount() {
		return vertices.size();
	}

	/**
	 * Looks up a vertex.
	 * @param idx Vertex index
	 * @return Vertex
	 */
	public Vertex getIndexedVertex(int idx) {
		return vertices.get(idx);
	}

	/**
	 * Adds a vertex.
	 * @param v Vertex
	 */
	public void add(Vertex v) {
		Check.notNull(v);
		vertices.add(v);
	}

	/**
	 * Adds a list of vertices.
	 * @param list Vertex data
	 */
	public void addVertices(List<Vertex> list) {
		for(Vertex v : list) {
			add(v);
		}
	}

	/**
	 * Creates a bounding box from this mesh.
	 * @return Bounding box
	 */
	public BoundingBox getBounds() {
		return BoundingBox.of(vertices.stream().map(Vertex::getPosition));
	}

	/**
	 * Adds a quad comprised of two triangles.
	 * @param array Quad of vertices
	 */
	public void addQuad(Vertex[] array) {
		Check.notNull(array);
		if(array.length != 4) throw new IllegalArgumentException("Expected quad array");

		// Top-left triangle
		add(array[0]);
		add(array[1]);
		add(array[2]);

		// Bottom-right triangle
		switch(layout.getPrimitive()) {
		case TRIANGLES:
			add(array[2]);
			add(array[1]);
			add(array[3]);
			break;

		case TRIANGLE_STRIP:
			add(array[3]);
			break;

		default:
			throw new IllegalArgumentException("Invalid primitive for quad: " + layout.getPrimitive());
		}
	}

	/**
	 * Adds a quad of the given size at the origin of the X-Y plane.
	 * @param size Quad size
	 */
	public void addQuad(Quad quad) {
		addQuad(quad.getVertices());
	}

	/**
	 * @return Whether this mesh requires normals
	 */
	public final boolean hasNormals() {
		return layout.contains(DefaultBufferDataType.NORMALS);
	}

	/**
	 * Generates <b>all</b> vertex normals in this mesh.
	 */
	public void computeNormals() {
		// Init normals
		for(Vertex v : vertices) {
			v.setNormal(new Vector());
		}

		// Compute normals
		computeNormals(0, getFaceCount());
		normalize();
	}

	/**
	 * Generates vertex normals based on averaging adjacent vertices.
	 * @param start		Starting index
	 * @param end		End index
	 */
	public void computeNormals(int start, int end) {
		// Check mesh is ready
		if(!hasNormals()) throw new IllegalArgumentException("Normals not specified: " + layout);
		verify();

		// Check valid for this primitive
		final Primitive primitive = layout.getPrimitive();
		if(!primitive.hasNormals()) throw new IllegalArgumentException("Invalid primitive for normals: " + primitive);

		// Sum normals at each triangle vertex
		boolean even = true;
		final TriangleIterator itr = new TriangleIterator(this);
		while(itr.hasNext()) {
			// Get next triangle
			final Triangle tri = itr.next();

			// Sum normals at each corner
			for(int c = 0; c < 3; ++c) {
				// Builds vectors to other corners of this triangle
				final Vertex vertex = tri.getVertex(c);
				final Point pt = vertex.getPosition();
				final Vector u = getEdge(tri, c + 1, pt);
				final Vector v = getEdge(tri, c + 2, pt);

				// Update vertex normal
				if(even) {
					vertex.addNormal(u.cross(v));
				}
				else {
					vertex.addNormal(v.cross(u));
				}
			}

			// Swap normal direction
			if(primitive == Primitive.TRIANGLE_STRIP) {
				even = !even;
			}
		}
	}

	/**
	 * Calculate an edge vector of a triangle.
	 * @param tri		Triangle
	 * @param idx		Vertex index increment
	 * @param start		Vertex position
	 */
	private static Vector getEdge(Triangle tri, int idx, Point start) {
		final Point end = tri.getVertex(idx % 3).getPosition();
		return Vector.between(start, end);
	}

	/**
	 * Normalizes <b>all</b> mesh normals.
	 */
	public void normalize() {
		for(Vertex v : vertices) {
			final Vector n = v.getNormal().normalize();
			v.setNormal(n);
		}
	}

	/**
	 * @throws IllegalArgumentException if this mesh is not valid
	 */
	protected boolean isValid() {
		return layout.getPrimitive().isValidVertexCount(vertices.size());
	}

	/**
	 * @return Number of faces for the drawing primitive of this mesh
	 */
	public final int getFaceCount() {
		final int count = getVertexCount();
		return layout.getPrimitive().getFaceCount(count);
	}

	/**
	 * Resets this builder.
	 */
	public void reset() {
		vertices.clear();
	}

	/**
	 * Constructs a buffered mesh from this builder.
	 * @return Buffered mesh
	 */
	public BufferedMesh build() {
		// Create new buffered mesh
		final BufferedMesh mesh = new BufferedMesh(layout, vertices.size(), getIndexSize());

		// Buffer all data
		update(mesh);

		return mesh;
	}

	/**
	 * @throws IllegalArgumentException if this builder is not ready to be built or updated
	 */
	protected void verify() {
		if(!isValid()) throw new IllegalArgumentException("Invalid number of vertices: " + vertices.size());
	}

	/**
	 * @return Size of the index buffer or <b>zero</b> if no buffer
	 */
	protected Integer getIndexSize() {
		return null;
	}

	// TODO - link to buffer from here
	/**
	 * Updates all vertex data to the given buffered mesh.
	 * @param mesh Buffered mesh to update
	 */
	public void update(BufferedMesh mesh) {
		update(mesh, 0, vertices.size());
	}

	/**
	 * Updates vertex data in the given range.
	 * @param mesh		Buffered mesh to update
	 * @param start		Start index
	 * @param end		End index
	 */
	public void update(BufferedMesh mesh, int start, int end) {
		Check.zeroOrMore(start);
		if(end < start) throw new IllegalArgumentException("End must be after start vertex");
		if(end > vertices.size()) throw new IllegalArgumentException("Not enough vertices");
		verify();

		final int num = layout.getBufferLayout().size();
		for(int n = 0; n < num; ++n) {
			// Lookup layout and associated buffer
			final BufferLayout b = layout.getBufferLayout().get(n);
			final FloatBuffer fb = mesh.getVertexBuffers()[n];

			// Populate buffer segment
			fb.position(start * b.getSize());
			for(int v = start; v < end; ++v) {
				b.append(vertices.get(v), fb);
			}

			// Prepare buffer for rendering
			fb.rewind();
		}
	}

	@Override
	public String toString() {
		final ToString ts = new ToString(this);
		ts.append("layout", layout);
		ts.append("count", getFaceCount());
		return ts.toString();
	}
}
