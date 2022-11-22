package org.sarge.jove.model;

import static org.sarge.lib.util.Check.*;

import java.util.Optional;

import org.sarge.jove.common.*;

/**
 * A <i>buffered mesh</i> comprises buffered vertex data and an optional index.
 * @see DefaultMesh#buffer()
 * @author Sarge
 */
public class BufferedMesh extends AbstractMesh {
	private final int count;
	private final ByteSizedBufferable vertices, index;

	/**
	 * Constructor.
	 * @param primitive		Drawing primitive
	 * @param count			Draw count
	 * @param layout		Vertex layout
	 * @param vertices		Vertex buffer
	 * @param index			Optional index buffer
	 */
	public BufferedMesh(Primitive primitive, int count, CompoundLayout layout, ByteSizedBufferable vertices, ByteSizedBufferable index) {
		super(primitive, layout);
		this.count = zeroOrMore(count);
		this.vertices = notNull(vertices);
		this.index = index;
	}

	/**
	 * Copy constructor.
	 * @param mesh			Underlying mesh
	 * @param vertices		Vertex buffer
	 * @param index			Optional index buffer
	 */
	public BufferedMesh(Mesh mesh, ByteSizedBufferable vertices, ByteSizedBufferable index) {
		this(mesh.primitive(), mesh.count(), mesh.layout(), vertices, index);
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public boolean isIndexed() {
		return index != null;
	}

	/**
	 * @return Vertex buffer
	 */
	public ByteSizedBufferable vertices() {
		return vertices;
	}

	/**
	 * @return Index buffer
	 */
	public Optional<ByteSizedBufferable> index() {
		return Optional.ofNullable(index);
	}
}
