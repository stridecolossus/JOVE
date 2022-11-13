package org.sarge.jove.model;

import static org.sarge.lib.util.Check.*;

import java.util.Optional;

import org.sarge.jove.common.*;

/**
 * A <i>buffered model</i> is a renderable mesh comprised of buffered vertex data and an optional index.
 * @see DefaultModel#buffer()
 * @author Sarge
 */
public class BufferedModel extends AbstractModel {
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
	public BufferedModel(Primitive primitive, int count, Layout layout, ByteSizedBufferable vertices, ByteSizedBufferable index) {
		super(primitive, layout);
		this.count = zeroOrMore(count);
		this.vertices = notNull(vertices);
		this.index = index;
	}

	/**
	 * Copy constructor.
	 * @param model			Underlying model
	 * @param vertices		Vertex buffer
	 * @param index			Optional index buffer
	 */
	public BufferedModel(Model model, ByteSizedBufferable vertices, ByteSizedBufferable index) {
		this(model.primitive(), model.count(), model.layout(), vertices, index);
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
